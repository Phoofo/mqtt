package com.NettyApplication.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.dto.AirOperationDto;
import com.NettyApplication.listen.DtuManage;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.tool.MessageProducer;
import com.NettyApplication.toolmodel.RedisMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private DtuManage dtuManage;
    @Resource
    private IDeviceInfoService iDeviceInfoService;
    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource
    MessageProducer messageProducer;

    @Operation(description = "操作空调:/查询/开机/关机/制冷/制热/除湿")
    @PostMapping("/setAir")
    public ResponseEntity<String> setAir(@RequestBody AirOperationDto dto) {
        Assert.notNull(dto.getDeviceId(), "设备编码不能为空");
        Assert.notNull(dto.getControlId(), "主板编号不能为空");
        Assert.notNull(dto.getOperation(), "空调操作不能为空");
        Assert.notNull(dto.getDeviceTypeId(), "设备类型不能为空");
        Assert.isTrue(dto.getDeviceTypeId() == 1L, "设备类型不为空调");
        // 报文封装
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
        // 主板编号
        Short s = dto.getControlId();

        //封装redis记录
        String key = dto.getControlId().toString() + dto.getDeviceTypeId().toString() + dto.getDeviceId().toString();
        RedisMessage redisMessage = new RedisMessage();
        redisMessage.setMsgBytes(msgBytes);
        redisMessage.setOperation(dto.getOperation());
        redisMessage.setDeviceId(dto.getDeviceId());
        redisMessage.setControlId(dto.getControlId());
        //key封装
        redisMessage.setKey(key);
        redisMessage.setType(dto.getDeviceTypeId());
        Object o = redisTemplate.opsForValue().get(s.toString());
        if (ObjectUtil.isNotNull(o)) {
            JSONObject jsonObject = JSONUtil.parseObj(o.toString());
            HashMap<String, Object> hashMap = new HashMap<>(jsonObject);
            hashMap.put(key, JSONUtil.toJsonStr(redisMessage));
            redisTemplate.opsForValue().set(s.toString(), JSONUtil.toJsonStr(hashMap));
            //主板队列数加一
            messageProducer.removeValue("controlIds", dto.getControlId().toString());
            messageProducer.incrementValueAccessCount("controlIds", dto.getControlId().toString(), hashMap.size());
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put(key, JSONUtil.toJsonStr(redisMessage));
            redisTemplate.opsForValue().set(s.toString(), JSONUtil.toJsonStr(map));
            // 设置硬件的状态
            dtuManage.sendMsg(msgBytes, s, dto.getDeviceId(), dto.getOperation(), dto.getDeviceTypeId());
            //主板队列数加一
            messageProducer.incrementValueAccessCount("controlIds", dto.getControlId().toString(), 1);
        }

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

        List<Byte> deviceIds = new ArrayList<>();
        if (dto.getOperationType() == 1) {//批量操作
            Assert.notNull(dto.getDeviceIds(), "设备编码集不能为空");
            deviceIds = dto.getDeviceIds();
        } else if (dto.getOperationType() == 2) {//一键操作
            deviceIds = iDeviceInfoService.list(Wrappers.lambdaQuery(DeviceInfo.class)
                    .eq(DeviceInfo::getControlId, dto.getControlId()))
                    .stream().map(DeviceInfo::getDeviceId).collect(Collectors.toList());
        }
        HashMap<String, Object> map = new HashMap<>();

        Object o = redisTemplate.opsForValue().get(dto.getControlId().toString());
        if (ObjectUtil.isNotNull(o)) {
            JSONObject jsonObject = JSONUtil.parseObj(o.toString());
            map = new HashMap<>(jsonObject);
        }

        HashMap<String, Object> finalMap = map;
        deviceIds.forEach(deviceId -> {
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
            RedisMessage redisMessage = new RedisMessage();
            redisMessage.setMsgBytes(msgBytes);
            redisMessage.setOperation(dto.getOperation());
            redisMessage.setDeviceId(deviceId);
            redisMessage.setControlId(dto.getControlId());
            //key封装
            String key = dto.getControlId().toString() + dto.getDeviceTypeId().toString() + deviceId.toString();
            redisMessage.setKey(key);
            redisMessage.setType(dto.getDeviceTypeId());

            finalMap.put(key, JSONUtil.toJsonStr(redisMessage));
        });
        redisTemplate.opsForValue().set(dto.getControlId().toString(), JSONUtil.toJsonStr(finalMap));
        //主板队列数加个数
        messageProducer.incrementValueAccessCount("controlIds", dto.getControlId().toString(), finalMap.size());
        if (ObjectUtil.isNull(o) && deviceIds.size() > 0) {//队列为空则马上发送一个
            byte[] msgBytes = {
                    (byte) Integer.parseInt("AA", 16),//开头
                    (byte) Integer.parseInt("01", 16),//设备类型:空调01
                    deviceIds.get(0),//设备编号
                    dto.getOperation(),//功能:01查询;02开机(自动);03关机;04制冷;05制热;06除湿
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("00", 16),//可拓展参数
                    (byte) Integer.parseInt("FE", 16) //结尾
            };
            // 设置硬件的状态
            dtuManage.sendMsg(msgBytes, dto.getControlId(), dto.getDeviceId(), dto.getOperation(), dto.getDeviceTypeId());
        }

        return ResponseEntity.ok("Success!");
    }
}
