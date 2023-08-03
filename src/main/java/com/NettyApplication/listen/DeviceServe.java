package com.NettyApplication.listen;

import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.service.IOperateLogService;
import com.NettyApplication.tool.HexConversion;
import com.NettyApplication.tool.MessageProducer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 硬件交互服务
 */
@Slf4j
@Component
public class DeviceServe {
    @Resource
    private IOperateLogService service;
    @Resource
    private IDeviceInfoService deviceInfoService;
    @Resource
    MessageProducer messageProducer;

    /**
     * 发送硬件指令
     *
     * @param msgBytes     操作报文
     * @param address      主控板地址字节
     * @param deviceId     设备字节
     * @param operation    操作字节
     * @param deviceTypeId 设备类型字节
     */
    public void sendMsg(byte[] msgBytes, Short address, Byte deviceId, Byte operation, Byte deviceTypeId) {
        //获取保存在 ChannelMap 的所有已连接的主控板通道信息
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if (CollectionUtils.isEmpty(channelDetail)) {
            //判断主控板通道信息是否为空，如果为空说明无主控板连接，则直接返回。
            return;
        }
        //遍历已连接的主控板通道信息集合，比对和需要发送设备指令的主控板
        ConcurrentHashMap.KeySetView<ChannelId, ConcurrentHashMap<String, Object>> channelIds = channelDetail.keySet();
        for (ChannelId channelId : channelIds) {
            Channel channel = (Channel) channelDetail.get(channelId).get("channel");

            // 判断是否活跃 如果主板断开了，在map里面删除掉了
            if (channel == null || !channel.isActive()) {
                ChannelMap.getChannelDetail().remove(channelId);
                log.info("客户端:{},连接已经中断", channelId);
                return;
            }
            // 如果设备地址（address）与通道详细信息中的地址匹配，则执行 指令发送 操作。
            if (address == (Short) channelDetail.get(channelId).get("address")) {
                ByteBuf buffer = Unpooled.buffer();
                log.info("开始发送报文:{}", channelId + ": " + HexConversion.byteArrayToHexString(msgBytes));
                buffer.writeBytes(msgBytes);
                channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("发送成功:{}", channelId);
                    } else {
                        log.info("发送失败:{}", channelId);
                    }
                });
            }
        }
    }


}
