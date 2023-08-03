package com.NettyApplication.task;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.OperateLog;
import com.NettyApplication.listen.ChannelMap;
import com.NettyApplication.listen.DtuManage;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ScheduledTasks {

    @Resource
    MessageProducer messageProducer;
    @Resource
    private IDeviceInfoService deviceInfoService;
    @Resource
    private IOperateLogService operateLogService;

    private static Map<String, String> hashMap = new HashMap<>();

    /**
     *
     */
//    @Scheduled(fixedRate = 2000) // 每隔2秒执行一次任务
    public void task() {

        System.out.println("定时任务执行时间->->  " + LocalDateTime.now());
        Map<String, String> map = messageProducer.getAllValueAccessCounts("controlIds");
        System.out.println("@@@-->   " + map);
        if (!hashMap.isEmpty()) {
            Map<String, String> allValueAccessCounts = messageProducer.getAllValueAccessCounts(null);
            for (String key : map.keySet()) {//主板轮询
                String s = map.get(key);
                String s1 = hashMap.get(key);
                if ((StringUtils.isNotEmpty(s) && StringUtils.isNotEmpty(s1)) && s.equals(s1)) {
                    //如果同一个主板与2秒前的待执行数一致
                    //则去获取主板下待执行队列，比对执行指令次数
                    Object value1 = messageProducer.getValue(key);
                    if (ObjectUtil.isNotNull(value1)) {
                        JSONObject jsonObject = JSONUtil.parseObj(value1.toString());
                        HashMap<String, Object> objectHashMap = new HashMap<>(jsonObject);
                        if (objectHashMap.isEmpty()) continue;
                        for (String mkey : objectHashMap.keySet()) {//设备轮询
                            Object o = objectHashMap.get(mkey);
                            RedisMessage redisMessage = JSONUtil.toBean(o.toString(), RedisMessage.class);
                            if (ObjectUtil.isNull(redisMessage)) continue;
                            //获取重试次数
                            String value = allValueAccessCounts.get(redisMessage.getKey());
                            if ((StringUtils.isNotEmpty(value) && Integer.parseInt(value) < 3) || StringUtils.isEmpty(value)) {//重试小于3次
                                // 再次发送指令
                                sendMsg(redisMessage.getMsgBytes(), redisMessage.getControlId());
                                //增加次数
                                messageProducer.incrementValueAccessCount(null, mkey, 1);
                                break;
                            } else if (StringUtils.isNotEmpty(value) && Integer.parseInt(value) >= 3) {//重试大于等于3次
                                //获取设备信息
                                DeviceInfo one = deviceInfoService.getOne(Wrappers.lambdaQuery(DeviceInfo.class)
                                        .eq(DeviceInfo::getControlId, redisMessage.getControlId())//主板编码
                                        .eq(DeviceInfo::getDeviceId, redisMessage.getDeviceId())//设备编码
                                        .eq(DeviceInfo::getDeviceTypeId, redisMessage.getType())//设备类型
                                );
                                //更新设备信息
                                if (ObjectUtil.isNotNull(one)) {
                                    one.setIsConnect(Boolean.FALSE);
                                    one.setLastModifiedDate(LocalDateTime.now());
                                    deviceInfoService.updateById(one);
                                }
                                //redis缓存移除逻辑
                                objectHashMap.remove(mkey);
                                if (objectHashMap.isEmpty()) {
                                    messageProducer.delete(key);
                                } else {
                                    messageProducer.setValue(key, JSONUtil.toJsonStr(objectHashMap));
                                }
                                messageProducer.removeValue(null, mkey);
                                messageProducer.removeValue("controlIds", key);
                                if (!objectHashMap.isEmpty())
                                    messageProducer.incrementValueAccessCount("controlIds", key, objectHashMap.size());

                                //  保存失联日志
                                OperateLog operateLog = new OperateLog();
                                operateLog.setControlId(redisMessage.getControlId());
                                operateLog.setDeviceTypeId(redisMessage.getType());
                                operateLog.setDeviceId(redisMessage.getDeviceId());
                                operateLog.setWriteBack(Boolean.FALSE);
                                operateLogService.save(operateLog);
                            }
                        }
                    }

                }

            }

        }

        hashMap = map;

    }


    public void sendMsg(byte[] msgBytes, Short address) {
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if (CollectionUtils.isEmpty(channelDetail)) {
            return;
        }
        ConcurrentHashMap.KeySetView<ChannelId, ConcurrentHashMap<String, Object>> channelIds = channelDetail.keySet();
        for (ChannelId channelId : channelIds) {
            Channel channel = (Channel) channelDetail.get(channelId).get("channel");
            // 判断是否活跃
            if (channel == null || !channel.isActive()) {
                ChannelMap.getChannelDetail().remove(channelId);
                log.info("客户端:{},连接已经中断", channelId);
                return;
            }

            if (ObjectUtil.isNull(channelDetail.get(channelId).get("address"))) {
                log.error("主板{},无主板注册信息", address);
                throw new IllegalArgumentException("无主板注册信息");
            }
            // 指令发送
            if (address == (Short) channelDetail.get(channelId).get("address")) {
                ByteBuf buffer = Unpooled.buffer();
                log.info("开始发送报文:{}", channelId + "：" + HexConversion.byteArrayToHexString(msgBytes));
                buffer.writeBytes(msgBytes);
                channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("客户端:{},回写成功:{}", channelId, HexConversion.byteArrayToHexString(msgBytes));
                    } else {
                        log.info("客户端:{},回写失败:{}", channelId, HexConversion.byteArrayToHexString(msgBytes));
                    }
                });
            }
        }
    }


}
