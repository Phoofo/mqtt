package com.NettyApplication.listen;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
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


    public void sendMsg(byte[] msgBytes,String remoteaddress){
        ConcurrentHashMap<ChannelId, Channel> channelMap = ChannelMap.getChannelMap();
        if(CollectionUtils.isEmpty(channelMap)){
            return;
        }
        ConcurrentHashMap.KeySetView<ChannelId, Channel> channelIds = channelMap.keySet();
        for(ChannelId channelId : channelIds){
            Channel channel = ChannelMap.getChannelByName(channelId);
            // 判断是否活跃
            if(channel==null || !channel.isActive()){
                ChannelMap.getChannelMap().remove(channelId);
                log.info("客户端:{},连接已经中断",channelId);
                return ;
            }

            System.out.println(channel.remoteAddress());
            System.out.println(remoteaddress);
            // 指令发送
            if(channel.remoteAddress().toString().equals(remoteaddress)) {
                ByteBuf buffer = Unpooled.buffer();
                log.info("开始发送报文:{}", channelId + "：" + Arrays.toString(msgBytes));
                buffer.writeBytes(msgBytes);
                channel.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("客户端:{},回写成功:{}", channelId, Arrays.toString(msgBytes));
                    } else {
                        log.info("客户端:{},回写失败:{}", channelId, Arrays.toString(msgBytes));
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
    public void deleteInactiveConnections(){
        ConcurrentHashMap<ChannelId, Channel> channelMap = ChannelMap.getChannelMap();
        if(!CollectionUtils.isEmpty(channelMap)){
            for (Map.Entry<ChannelId, Channel> next : channelMap.entrySet()) {
                ChannelId channelId = next.getKey();
                Channel channel = next.getValue();
                if (!channel.isActive()) {
                    channelMap.remove(channelId);
                    log.info("客户端:{},连接已经中断",channelId);
                }
            }
        }
    }
}


