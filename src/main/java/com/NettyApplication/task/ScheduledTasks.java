package com.NettyApplication.task;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.Control;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.OperateLog;
import com.NettyApplication.listen.ChannelMap;
import com.NettyApplication.listen.DeviceServe;
import com.NettyApplication.listen.DtuManage;
import com.NettyApplication.service.IControlService;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.service.IOperateLogService;
import com.NettyApplication.tool.HexConversion;
import com.NettyApplication.tool.MessageProducer;
import com.NettyApplication.toolmodel.RedisMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduledTasks {

    @Resource
    MessageProducer messageProducer;
    @Resource
    private DeviceServe deviceServe;
    @Resource
    private IDeviceInfoService deviceInfoService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private IControlService controlService;

    private static Map<String, String> hashMap = new HashMap<>();

    /**
     *
     */
    @Scheduled(fixedRate = 2000) // 每隔2秒执行一次任务
    public void task() {

        System.out.println("定时任务执行时间->->  " + LocalDateTime.now());
        //获取所有已连接的主板ID
        List<Short> controlIds = controlService.list(Wrappers.lambdaQuery(Control.class)
                .eq(Control::getConnectionStatus, Boolean.TRUE)
        ).stream().map(Control::getId).collect(Collectors.toList());

        //获取所有已连接的主板的指令队列
        ListOperations listControl = redisTemplate.opsForList();//获取对Redis队列操作的实例。

        controlIds.forEach(controlId -> {
            //获取主板下所有指令的 hash key值
            List<String> keys = listControl.range(controlId.toString(), 0, -1);
            //主板指令不为空，则遍历比对其指令是否2秒无回复
            if (keys.size() != 0) {
                HashOperations<String, Object, Object> devices = redisTemplate.opsForHash();//获取对Redis哈希表进行操作的实例
                for (String key : keys) {
                    //获取该指令的发送时间
                    Object timeObj = devices.get(key, "time");
                    LocalDateTime time = null;
                    if (timeObj != null && timeObj instanceof LocalDateTime) {//有发送时间,判断是异常还是重试
                        time = (LocalDateTime) timeObj;
                        //是否在指定时间内消费
                        Duration duration = Duration.between(LocalDateTime.now(), time);
                        if (duration.getSeconds() > 2) {//大于两秒未消费
                            int number = (int) devices.get(key, "number");
                            if (number > 0) {//还有重试机会
                                //获取发送指令参数
                                byte[] message = (byte[]) devices.get(key, "message");
                                Byte deviceTypeId = (Byte) devices.get(key, "deviceTypeId");
                                Byte deviceId = (Byte) devices.get(key, "deviceId");
                                Byte nextOperation = (Byte) devices.get(key, "operation");
                                devices.put(key, "number", number - 1);//重试次数减一
                                devices.put(key, "time", LocalDateTime.now());//重设发送时间
                                //发送指令
                                deviceServe.sendMsg(message, controlId, deviceId, nextOperation, deviceTypeId);
                                //跳出本次循环，一台主板一次只能发一条指令
                                break;
                            } else {//无重试机会
                                SetOperations<String, Object> keySets = redisTemplate.opsForSet();//获取对Set表进行操作的实例
                                //移除相关set
                                keySets.remove("id", key);
                                //移除相关hash
                                devices.delete(key);
                                //移除相关list，首先获取到key的索引
                                int index = -1;
                                for (int i = 0; i < keys.size(); i++) {
                                    if (keys.get(i).equals(key)) {
                                        index = i;
                                        break;
                                    }
                                }
                                //消费该指令
                                listControl.remove(controlId, index, key);
                                //至此该设备重试无响应记录 todo 异常操作日志

                                //准备执行下一条指令,
                                //查询是否还有下条指令，没有跳出
                                if (listControl.range(controlId.toString(), 0, -1).size() == 0) break;
                                String nextKey = (String) listControl.range(controlId.toString(), 0, 0).get(0);
                                //获取发送指令参数
                                byte[] message = (byte[]) devices.get(nextKey, "message");
                                Byte deviceTypeId = (Byte) devices.get(nextKey, "deviceTypeId");
                                Byte deviceId = (Byte) devices.get(nextKey, "deviceId");
                                Byte nextOperation = (Byte) devices.get(nextKey, "operation");
                                //处理redis
                                keySets.add("id", nextKey);//保存set信息
                                devices.put(nextKey, "number", number - 1);//重试次数减一
                                devices.put(nextKey, "time", LocalDateTime.now());//设置发送时间
                                //发送指令
                                deviceServe.sendMsg(message, controlId, deviceId, nextOperation, deviceTypeId);
                                //跳出本次循环，一台主板一次只能发一条指令
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

}
