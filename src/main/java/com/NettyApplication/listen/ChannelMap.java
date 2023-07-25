package com.NettyApplication.listen;

import com.NettyApplication.toolmodel.TenByteEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.util.CollectionUtils;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述: 管理通道Map类
 *
 * @Author yb
 * @Date 2023/05/08
 */
public class ChannelMap {

    /**
     * 管理一个全局map，保存连接进服务端的通道数量
     */
    private static final ConcurrentHashMap<ChannelId, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(1000);

    public static ConcurrentHashMap<ChannelId, Channel> getChannelMap() {
        return CHANNEL_MAP;
    }

    /**
     * 管理一个全局map，保存连接进服务端的通道和硬件的详细信息
     */
    private static final ConcurrentHashMap<ChannelId,ConcurrentHashMap<String, Object>> CHANNEL_detail = new ConcurrentHashMap<>(1000);
    public static ConcurrentHashMap<ChannelId,ConcurrentHashMap<String, Object>> getChannelDetail() {
        return CHANNEL_detail;
    }

    /**
     *  获取指定name的channel
     */
    public static Channel getChannelByName(ChannelId channelId){
        if(CollectionUtils.isEmpty(CHANNEL_MAP)){
            return null;
        }
        return CHANNEL_MAP.get(channelId);
    }

    public static Channel getChannelByName1(ChannelId channelId){
        if(CollectionUtils.isEmpty(CHANNEL_detail)){
            return null;
        }
        ConcurrentHashMap<String, Object> stringObjectConcurrentHashMap = CHANNEL_detail.get(channelId);
        return (Channel)stringObjectConcurrentHashMap.get(channelId);
    }

    /**
     *  将通道中的消息推送到每一个客户端
     */
    public static boolean pushNewsToAllClient(String obj){
        if(CollectionUtils.isEmpty(CHANNEL_MAP)){
            return false;
        }
        for(ChannelId channelId: CHANNEL_MAP.keySet()) {
            Channel channel = CHANNEL_MAP.get(channelId);
            channel.writeAndFlush(new TextWebSocketFrame(obj));
        }
        return true;
    }

    /**
     *  将channel和对应的name添加到ConcurrentHashMap
     */
    public static void addChannel(ChannelId channelId,Channel channel){
        CHANNEL_MAP.put(channelId,channel);
    }

    /**
     *  移除掉name对应的channel
     */
    public static boolean removeChannelByName(ChannelId channelId){
        if(CHANNEL_MAP.containsKey(channelId)){
            CHANNEL_MAP.remove(channelId);
            return true;
        }
        return false;
    }
    public static void addChannelOrTenByteEntity(ChannelId channelId,ConcurrentHashMap<String, Object> TenByteEntity){
        CHANNEL_detail.put(channelId,TenByteEntity);
    }

    public static boolean removeChannelByChannelId(ChannelId channelId){
        if(CHANNEL_detail.containsKey(channelId)){
            CHANNEL_detail.remove(channelId);
            return true;
        }
        return false;
    }

}


