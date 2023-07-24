package com.NettyApplication.simulateclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class GroupChatClient {

    //属性
    private final String host;
    private final int port;

    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        //2, 在 run() 方法中，创建一个 NioEventLoopGroup 对象作为事件循环组。
        EventLoopGroup group = new NioEventLoopGroup();

        try {


            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            //得到pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            //加入相关handler
                            pipeline.addLast("decoder", new MyDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自定义的handler
                            pipeline.addLast(new GroupChatClientHandler());
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true); // 设置长连接;

            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            //得到channel
            Channel channel = channelFuture.channel();
            System.out.println("通道ip和端口号-------" + channel.localAddress() + "--------");
            //客户端需要输入信息，创建一个扫描器
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                //通过channel 发送到服务器端 AA01016400001FF
//                byte[] msgBytes = {(byte) 0xAA, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, (byte) 0xFF};
                byte[] msgBytes = new byte[msg.length() / 2];

                for (int i = 0; i < msg.length(); i += 2) {
                    if (i + 2 <= msg.length()) {
                        String substring = msg.substring(i, i + 2);
                        int decimalValue = Integer.parseInt(substring, 16);
                        byte byteValue = (byte) decimalValue;
                        msgBytes[i / 2] = byteValue;
                    } else {
                        // 处理索引超出范围的情况
                    }

                }
                channel.writeAndFlush(Unpooled.wrappedBuffer(msgBytes)).sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
//        new GroupChatClient("127.0.0.1", 20000).run();
        //
//        new GroupChatClient("316n689m11.zicp.fun", 12459).run();
        //1, 创建 GroupChatClient 类，并设置服务器的主机名（host）和端口号（port）作为属性。
        new GroupChatClient("127.0.0.1", 20000).run();
    }
}
