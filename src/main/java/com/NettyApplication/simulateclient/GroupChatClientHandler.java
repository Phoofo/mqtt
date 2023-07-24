package com.NettyApplication.simulateclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class GroupChatClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      System.out.println(msg);
    }

    private void parseReceivedBytes(byte[] receivedBytes) {
        // 解析接收到的字节数组，按照协议规定的格式进行处理
        // 下面是一个简单的示例，假设接收到的字节数组的长度是固定的

        // 解析字节数组中的每个字段
        byte field1 = receivedBytes[0];
        byte field2 = receivedBytes[1];
        byte field3 = receivedBytes[2];
        // ...

        // 打印解析结果
        System.out.println("Parsed fields: ");
        System.out.println("Field 1: " + field1);
        System.out.println("Field 2: " + field2);
        System.out.println("Field 3: " + field3);
        // ...
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}