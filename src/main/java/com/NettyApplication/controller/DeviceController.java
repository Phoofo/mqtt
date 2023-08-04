package com.NettyApplication.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.dto.AirOperationDto;
import com.NettyApplication.listen.DeviceServe;
import com.NettyApplication.listen.DtuManage;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.tool.MessageProducer;
import com.NettyApplication.toolmodel.RedisMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备操作模块
 * </p>
 *
 * @author p
 * @since 2023-07-25
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/Device")
@Tag(name = "Device", description = "设备操作")
public class DeviceController {

    @Resource
    private DeviceServe deviceServe;
    @Resource
    private IDeviceInfoService iDeviceInfoService;
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    @Operation(description = "操作空调:/查询/开机/关机/制冷/制热/除湿")
    @PostMapping("/setAir")
    public ResponseEntity<String> setAir(@RequestBody AirOperationDto dto) {
        Assert.notNull(dto.getDeviceId(), "设备编码不能为空");
        Assert.notNull(dto.getControlId(), "主板编号不能为空");
        Assert.notNull(dto.getOperation(), "空调操作不能为空");
        Assert.notNull(dto.getDeviceTypeId(), "设备类型不能为空");
        Assert.isTrue(dto.getDeviceTypeId() == 1L, "设备类型不为空调");
        //获取主控板信息
        Short controlId = dto.getControlId();
        // 指令报文封装
        byte[] msgBytes = {
                (byte) Integer.parseInt("AA", 16),//开头
                (byte) Integer.parseInt("01", 16),//设备类型:空调01
                dto.getDeviceId(),//设备编号
                dto.getOperation(),//功能:01查询;02开机(自动);03关机;04制冷;05制热;06除湿
                (byte) Integer.parseInt("00", 16),//可拓展参数
                (byte) Integer.parseInt("00", 16),//可拓展参数
                (byte) Integer.parseInt("00", 16),//可拓展参数
                (byte) Integer.parseInt("FE", 16) //结尾
        };

        /**
         *  1、前端发送操作指令，首先查询set是否有该设备
         *          one：有   直接返回不能操作
         *          two：没有    1、直接发送到硬件 2、list排队、在hash里面记录次数、放到set做重复性操作
         */
        // key是 主控板ID:硬件类型:硬件编号 组合而成
        String key = controlId.toString() + ":" + dto.getDeviceTypeId().toString() + ":" + dto.getDeviceId().toString();
        // 通过set中该设备的值 进行判断 该设备是否正在处理其他指令
        SetOperations<String, Object> stringObjectSetOperations = redisTemplate.opsForSet();
        Boolean member = stringObjectSetOperations.isMember("id", key);
        // 有，则说明该设备正在处理其他指令
        if (member) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("指令正在处理，请不要重复操作!");
        // 没有，则准备发送redis缓存信息
        stringObjectSetOperations.add("id", key);//保存set信息
        ListOperations<String, Object> stringObjectListOperations = redisTemplate.opsForList();
        HashOperations<String, Object, Object> stringObjectObjectHashOperations = redisTemplate.opsForHash();
        // 判断redis该主板队列是否存在,和是否有其他设备正在占用主板
        boolean keyExists = redisTemplate.hasKey(controlId.toString());
        Long size = null;
        if (keyExists) {
            size = stringObjectListOperations.size(controlId.toString());
            //该主板队列存在，且为空，则直接发送指令
            if (size == 0 || size == null)
                //发送时间戳保存
                stringObjectObjectHashOperations.put(key, "time", LocalDateTime.now());//发送消息才设置发送时间
            deviceServe.sendMsg(msgBytes, controlId, dto.getDeviceId(), dto.getOperation(), dto.getDeviceTypeId());
        }
        //保存到该主板队列，方便顺序执行发送
        stringObjectListOperations.rightPush(controlId.toString(), key);

        //保存到主板设备hash，记录报文，指令和次数
        stringObjectObjectHashOperations.put(key, "operation", dto.getOperation());
        stringObjectObjectHashOperations.put(key, "deviceId", dto.getDeviceId());
        stringObjectObjectHashOperations.put(key, "deviceTypeId", dto.getDeviceTypeId());
        stringObjectObjectHashOperations.put(key, "number", (size == 0 || size == null) ? 2 : 3);//已发送记录2，未发生记录3
        stringObjectObjectHashOperations.put(key, "message", msgBytes);

        return ResponseEntity.ok("Success!");
    }

    /**
     * 批量操作空调:/查询/开机/关机/制冷/制热/除湿
     *
     * @param dto
     * @return
     */
    @Operation(description = "批量操作空调:/查询/开机/关机/制冷/制热/除湿")
    @PostMapping("/setAirBatch")
    public ResponseEntity<String> setAirBatch(@RequestBody AirOperationDto dto) {
        Assert.notNull(dto.getControlId(), "主板编号不能为空");
        Assert.notNull(dto.getOperation(), "空调操作不能为空");
        Assert.notNull(dto.getOperationType(), "操作类型不能为空");
        Assert.notNull(dto.getDeviceTypeId(), "设备类型不能为空");
        Assert.isTrue(dto.getDeviceTypeId() == 1L, "设备类型不为空调");
        //获取主控板信息
        Short controlId = dto.getControlId();

        List<Byte> deviceIds = new ArrayList<>();
        if (dto.getOperationType() == 1) {//批量操作
            Assert.notNull(dto.getDeviceIds(), "设备编码集不能为空");
            deviceIds = dto.getDeviceIds();
        } else if (dto.getOperationType() == 2) {//一键操作
            deviceIds = iDeviceInfoService.list(Wrappers.lambdaQuery(DeviceInfo.class)
                    .eq(DeviceInfo::getControlId, dto.getControlId()))
                    .stream().map(DeviceInfo::getDeviceId).collect(Collectors.toList());
        }
        SetOperations<String, Object> stringObjectSetOperations = redisTemplate.opsForSet();
        ListOperations<String, Object> listControl = redisTemplate.opsForList();
        HashOperations<String, Object, Object> stringObjectObjectHashOperations = redisTemplate.opsForHash();
        for (Byte deviceId : deviceIds) {
            // 报文封装
            byte[] msgBytes = {
                    (byte) Integer.parseInt("AA", 16),//开头
                    (byte) Integer.parseInt("01", 16),//设备类型:空调01
                    deviceId,//设备编号
                    dto.getOperation(),//功能:01查询;02开机(自动);03关机;04制冷;05制热;06除湿
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("FE", 16) //结尾
            };
            //key封装
            String key = dto.getControlId().toString() + dto.getDeviceTypeId().toString() + deviceId.toString();
            //判断该设备是否处理其他指令
            Boolean member = stringObjectSetOperations.isMember("id", key);
            // 有，则说明该设备正在处理其他指令直接跳过
            if (member) continue;
            //保存到主板设备hash，记录报文，指令和次数
            stringObjectObjectHashOperations.put(key, "operation", dto.getOperation());
            stringObjectObjectHashOperations.put(key, "deviceId", deviceId);
            stringObjectObjectHashOperations.put(key, "deviceTypeId", dto.getDeviceTypeId());
            stringObjectObjectHashOperations.put(key, "number", 3);//已发送记录2，未发生记录3
            stringObjectObjectHashOperations.put(key, "message", msgBytes);
            //加入队列
            listControl.rightPush(controlId.toString(), key);
        }
        if (listControl.range(controlId.toString(), 0, -1).size() > 0) {
            String firstKey = (String) listControl.range(controlId.toString(), 0, 0).get(0);
            System.out.println("队列的第一个key为: " + firstKey);
            Object timeObj = stringObjectObjectHashOperations.get(firstKey, "time");
            LocalDateTime time = null;
            if (timeObj != null && timeObj instanceof LocalDateTime) {
                //有发送时间不处理，等队列完成自己处理下一个
                return ResponseEntity.ok("Success!");
            } else {
                //无时间则说明队列无指令执行，就直接发送第一条指令
                byte[] message = (byte[]) stringObjectObjectHashOperations.get(firstKey, "message");
                Byte deviceTypeId = (Byte) stringObjectObjectHashOperations.get(firstKey, "deviceTypeId");
                Byte deviceId = (Byte) stringObjectObjectHashOperations.get(firstKey, "deviceId");
                Byte nextOperation = (Byte) stringObjectObjectHashOperations.get(firstKey, "operation");
                int number = (int) stringObjectObjectHashOperations.get(firstKey, "number");
                System.out.println("message@@  -> " + message);
                System.out.println("deviceTypeId@@  -> " + deviceTypeId);
                System.out.println("nextOperation@@  -> " + nextOperation);
                System.out.println("number@@  -> " + number);
                System.out.println("deviceId@@  -> " + deviceId);
                //发送消息,设置发送时间
                stringObjectSetOperations.add("id", firstKey);//保存set信息
                stringObjectObjectHashOperations.put(firstKey, "time", LocalDateTime.now());
                stringObjectObjectHashOperations.put(firstKey, "number", number - 1);//重试次数减一
                deviceServe.sendMsg(message, controlId, deviceId, nextOperation, deviceTypeId);
            }
        }

        return ResponseEntity.ok("Success!");
    }
}
