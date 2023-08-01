package com.NettyApplication.task;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.listen.ChannelMap;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.tool.HexConversion;
import com.NettyApplication.tool.MessageProducer;
import com.NettyApplication.toolmodel.RedisMessage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ScheduledTasks {

    @Resource
    MessageProducer messageProducer;
    @Resource
    private IDeviceInfoService deviceInfoService;

    /**
     *
     */
    @Scheduled(fixedRate = 2000) // 每隔2秒执行一次任务
    public void task() {

        System.out.println("定时任务执行时间->->  " + LocalDateTime.now());
        Map<String, String> map = messageProducer.getAllValueAccessCounts();
        System.out.println("@@@-->   " + map);
        for (String key : map.keySet()) {
            Object value1 = messageProducer.getValue(key);
            RedisMessage redisMessage = JSONUtil.toBean(value1.toString(), RedisMessage.class);
            //获取重试次数
            String value = map.get(key);
            if (Integer.parseInt(value) < 3) {//重试小于3次
                // 再次发送指令
                sendMsg(redisMessage.getMsgBytes(), redisMessage.getControlId());
                //增加次数
                messageProducer.incrementValueAccessCount(key);
            } else {//重试大于等于3次
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
                log.warn("主板{},设备类型为{}的设备{}已失去连接", redisMessage.getControlId(),
                        redisMessage.getType(), redisMessage.getDeviceId());
            }
        }
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
