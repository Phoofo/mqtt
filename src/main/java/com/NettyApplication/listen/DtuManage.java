package com.NettyApplication.listen;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.OperateLog;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述: 定时发送Dtu报文
 *
 * @Author keLe
 * @Date 2022/8/29
 */
@Slf4j
@Component
public class DtuManage {
    @Resource
    private IOperateLogService service;
    @Resource
    private IDeviceInfoService deviceInfoService;
    @Resource
    MessageProducer messageProducer;

    public void sendMsg(byte[] msgBytes, Short address, Byte deviceId, Byte operation, Byte deviceTypeId) {
//        ConcurrentHashMap<ChannelId, Channel> channelMap = ChannelMap.getChannelMap();
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if (CollectionUtils.isEmpty(channelDetail)) {
            return;
        }
//        ConcurrentHashMap.KeySetView<ChannelId, Channel> channelIds = channelMap.keySet();
        ConcurrentHashMap.KeySetView<ChannelId, ConcurrentHashMap<String, Object>> channelIds = channelDetail.keySet();
        for (ChannelId channelId : channelIds) {
//            Channel channel = ChannelMap.getChannelByName1(channelId);
            Channel channel = (Channel) channelDetail.get(channelId).get("channel");
            // 判断是否活跃
            if (channel == null || !channel.isActive()) {
                ChannelMap.getChannelDetail().remove(channelId);
                log.info("客户端:{},连接已经中断", channelId);
                return;
            }

            System.out.println("netty中的IP" + channel.remoteAddress());
            log.info("address请求板编号====" + address);
            log.info(channelDetail.toString());
            log.info("address注册包@@@@@@@" + channelDetail.get(channelId).get("address"));
            if (ObjectUtil.isNull(channelDetail.get(channelId).get("address"))) {
                log.error("主板{},无主板注册信息", address);
                throw new IllegalArgumentException("无主板注册信息");
            }
            // 指令发送
            if (address == (Short) channelDetail.get(channelId).get("address")) {
                ByteBuf buffer = Unpooled.buffer();
                log.info("开始发送报文:{}", channelId + "：" + HexConversion.byteArrayToHexString(msgBytes));
                buffer.writeBytes(msgBytes);
                setValue(address, deviceId, deviceTypeId);
//                recordSending(msgBytes, address);
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

    private void setValue(Short address, Byte deviceId, Byte deviceTypeId) {
        String key = address.toString() + deviceTypeId.toString()
                + deviceId.toString();
        //增加值的访问次数
        messageProducer.incrementValueAccessCount(null, key, 1);
    }

    /**
     * 记录发送指令防止丢包
     *
     * @param msgByte 操作报文
     * @param address 操作主板编码
     */
    private void recordSending(byte[] msgByte, short address) {
        long count = service.count(Wrappers.lambdaQuery(OperateLog.class)
                .eq(OperateLog::getControlId, address)
                .eq(OperateLog::getDeviceId, msgByte[2])
                .eq(OperateLog::getDeviceTypeId, msgByte[1])
                .eq(OperateLog::getWriteBack, false)
        );
        if (count >= 3) {
            //获取设备信息
            DeviceInfo one = deviceInfoService.getOne(Wrappers.lambdaQuery(DeviceInfo.class)
                    .eq(DeviceInfo::getControlId, address)//主板编码
                    .eq(DeviceInfo::getDeviceId, msgByte[2])//设备编码
                    .eq(DeviceInfo::getDeviceTypeId, msgByte[1])//设备类型
            );
            //更新设备信息
            if (ObjectUtil.isNotNull(one)) {
                one.setIsConnect(Boolean.FALSE);
                one.setLastModifiedDate(LocalDateTime.now());
                deviceInfoService.updateById(one);
            }
            log.error("主板{},设备类型为{}的设备{}已失去连接", address, msgByte[1], msgByte[2]);

        } else {
            OperateLog operateLog = new OperateLog();
            operateLog.setControlId(address);
            operateLog.setDeviceId(msgByte[2]);
            operateLog.setDeviceTypeId(msgByte[1]);
            operateLog.setWriteBack(false);
            service.save(operateLog);
        }

    }

    /**
     * 功能描述: 定时删除不活跃的连接
     *
     * @return void
     * @Author keLe
     * @Date 2022/8/26
     */
//    @Scheduled(fixedDelay = 5000) // 每隔5秒执行一次
    public void deleteInactiveConnections() {
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if (!CollectionUtils.isEmpty(channelDetail)) {
            for (Map.Entry<ChannelId, ConcurrentHashMap<String, Object>> next : channelDetail.entrySet()) {

                ChannelId channelId = next.getKey();
                Channel channel = (Channel) next.getValue().get("channel");
                if (!channel.isActive()) {
                    channelDetail.remove(channelId);
                    log.info("客户端:{},连接已经中断", channelId);
                }
            }
        }
    }

}


