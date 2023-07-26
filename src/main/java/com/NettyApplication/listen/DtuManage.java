package com.NettyApplication.listen;

import com.NettyApplication.tool.HexConversion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.Arrays;
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


    public void sendMsg(byte[] msgBytes,short address){
//        ConcurrentHashMap<ChannelId, Channel> channelMap = ChannelMap.getChannelMap();
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if(CollectionUtils.isEmpty(channelDetail)){
            return;
        }
//        ConcurrentHashMap.KeySetView<ChannelId, Channel> channelIds = channelMap.keySet();
        ConcurrentHashMap.KeySetView<ChannelId, ConcurrentHashMap<String, Object>> channelIds = channelDetail.keySet();
        for(ChannelId channelId : channelIds){
//            Channel channel = ChannelMap.getChannelByName1(channelId);
            Channel channel = (Channel)channelDetail.get(channelId).get("channel");
            // 判断是否活跃
            if(channel==null || !channel.isActive()){
                ChannelMap.getChannelDetail().remove(channelId);
                log.info("客户端:{},连接已经中断",channelId);
                return ;
            }

            System.out.println("netty中的IP"+channel.remoteAddress());
            log.info("address========="+address);
            log.info(channelDetail.toString());
            log.info("address11111111"+channelDetail.get(channelId).get("address"));
            // 指令发送
            if(address == (Short)channelDetail.get(channelId).get("address")) {
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

    /**
     * 功能描述: 定时删除不活跃的连接
     * @Author keLe
     * @Date 2022/8/26
     * @return void
     */
    @Scheduled(fixedDelay = 5000) // 每隔5秒执行一次
    public void deleteInactiveConnections(){
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if(!CollectionUtils.isEmpty(channelDetail)){
            for (Map.Entry<ChannelId, ConcurrentHashMap<String, Object>> next : channelDetail.entrySet()) {

                ChannelId channelId = next.getKey();
                Channel channel = (Channel)next.getValue().get("channel");
                if (!channel.isActive()) {
                    channelDetail.remove(channelId);
                    log.info("客户端:{},连接已经中断",channelId);
                }
            }
        }
    }
}


