package com.NettyApplication.listen;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Component;


/**
 * 功能描述: 服务端初始化，客户端与服务器端连接一旦创建，这个类中方法就会被回调，设置出站编码器和入站解码器
 *
 * @Author yb
 * @Date 2023/05/08
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //接收消息格式,使用自定义解析数据格式
        pipeline.addLast("decoder",new MyDecoder());
        //发送消息格式，使用自定义解析数据格式
        pipeline.addLast("encoder",new MyEncoder());

        //针对客户端，如果在1分钟时没有想服务端发送写心跳(ALL)，则主动断开
        //如果是读空闲或者写空闲，不处理,这里根据自己业务考虑使用
//        pipeline.addLast(new IdleStateHandler(600,0,0, TimeUnit.SECONDS));
        //自定义的空闲检测
        pipeline.addLast(new NettyServerHandler());
    }
}

