package com.NettyApplication.listen;

import com.NettyApplication.entity.HardWare;
import com.NettyApplication.entity.HardWareControl;
import com.NettyApplication.service.IHardWareService;
import com.NettyApplication.service.impl.HardWareControlServiceImpl;
import com.NettyApplication.service.impl.HardWareServiceImpl;
import com.NettyApplication.tool.HexConversion;
import com.NettyApplication.toolmodel.DatagramEntity;
import com.NettyApplication.toolmodel.EightByteEntity;
import com.NettyApplication.toolmodel.TenByteEntity;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述: netty服务端处理类
 *
 * @Author yb
 * @Date 2023/5/8
 */
@Slf4j
@Component
public class NettyServerHandler extends ChannelInboundHandlerAdapter implements ApplicationContextAware {
    /**
     * 功能描述: 有客户端连接服务器会触发此函数
     * @Author yb
     * @Date 2023/5/8
     * @param  ctx 通道
     * @return void
     */
    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().write(new byte[] {0x00});
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        int clientPort = insocket.getPort();
        //获取连接通道唯一标识
        ChannelId channelId = ctx.channel().id();
        //如果map中不包含此连接，就保存连接
        if (ChannelMap.getChannelMap().containsKey(channelId)) {
            log.info("客户端:{},是连接状态，连接通道数量:{} ",channelId,ChannelMap.getChannelMap().size());
        } else {
            log.info(ctx.toString());
            log.info(ctx.channel().toString());
            //才激活  保存连接
            ConcurrentHashMap<String, Object> objectObjectConcurrentHashMap = new ConcurrentHashMap<>();
            objectObjectConcurrentHashMap.put("channel",ctx.channel());
            ChannelMap.addChannelOrTenByteEntity(channelId, objectObjectConcurrentHashMap);
            log.info("客户端:{},连接netty服务器[IP:{}-->PORT:{}]",channelId, clientIp,clientPort);
            log.info("channelActive连接通道数量: {}",ChannelMap.getChannelDetail().size());
        }
    }

    /**
     * 功能描述: 有客户端终止连接服务器会触发此函数
     * @Author yb
     * @Date 2023/5/8
     * @param  ctx 通道处理程序上下文
     * @return void
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        ChannelId channelId = ctx.channel().id();
        log.info("断开1客户端:{},连接netty服务器[IP:{}-->PORT:{}]",channelId, clientIp,inSocket.getPort());
        //包含此客户端才去删除
        if (ChannelMap.getChannelDetail().containsKey(channelId)) {
            //删除连接
            ChannelMap.getChannelDetail().remove(channelId);
            log.info("断开2客户端:{},连接netty服务器[IP:{}-->PORT:{}]",channelId, clientIp,inSocket.getPort());
            log.info("channelInactive连接通道数量: " + ChannelMap.getChannelMap().size());
        }
    }

    /**
     * 功能描述: 有客户端发消息会触发此函数
     * @Author yb
     * @Date 2023/5/8
     * @param  ctx 通道处理程序上下文
     * @param  msg 客户端发送的消息
     * @return void
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("加载客户端报文,客户端id:{},客户端消息:{}",ctx.channel().id(), msg);

        if(msg instanceof TenByteEntity){
            //如果是10个字节的心跳包数据，解析心跳包，保存主控板的信息和硬件的信息
            TenByteEntity entity = (TenByteEntity) msg;
            log.info(entity.toString());
            //保存主机地址
            ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
            ConcurrentHashMap<String, Object> stringObjectConcurrentHashMap = channelDetail.get(ctx.channel().id());
            stringObjectConcurrentHashMap.put("address",entity.getMainboardAddress());
        }else if(msg instanceof EightByteEntity){
            //如果是8个字节数据硬件状态，这是硬件返回的信息
            EightByteEntity entity = (EightByteEntity) msg;
            log.info(entity.toString());

        }
//        ByteBuf response = processRequest(msg,ctx);



        // 将响应写回给客户端
        // ctx.writeAndFlush(response);
    }

    private ByteBuf processRequest(DatagramEntity request,ChannelHandlerContext ctx) {

        log.info("报文消息:{}",request);
        /*
        * 业务逻辑处理
        * */
        //根据IP、端口和地址设置硬件的状态
        String socketAddress = ctx.channel().remoteAddress().toString();
//        Map<String, String> map = new HashMap<>();
//        for (Integer i = 0; i < request.length(); i += 2) {
//            String substring = request.substring(i, i + 2);
//            map.put(i.toString(),substring);
//        }
        // 截取 IP 地址部分
        String ip = socketAddress.substring(1, socketAddress.indexOf(":"));

        // 截取端口号部分
        String port = socketAddress.substring(socketAddress.indexOf(":") + 1);
        //HardWareControlServiceImpl hardWareControlServiceImpl = context.getBean(HardWareControlServiceImpl.class);
//        try{
//            //根据IP和端口号查询主控板的id
////        Integer id = hardWareControlServiceImpl.getOne(Wrappers.lambdaQuery(HardWareControl.class).eq(HardWareControl::getIp, ip)
////                .eq(HardWareControl::getPort, port)).getId();
//        // 使用ApplicationContext获取Bean实例
//        HardWareServiceImpl bean = context.getBean(HardWareServiceImpl.class);
//
//        // 跟俊主控板的id和硬件编号处理设备状态
//            HardWare one = bean.getOne(Wrappers.lambdaQuery(HardWare.class)
//                    .eq(HardWare::getNumber, map.get("2"))
//            );
//            one.setState(map.get("6"));
//            bean.updateById(one);
//        }catch(Exception e){
//            log.info(e.toString());
//        }


             String response ="123";
        ByteBuf respBuf = Unpooled.copiedBuffer(response, CharsetUtil.UTF_8);
        return  respBuf;
    }

   @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
       // 刷新数据并关闭连接
       ctx.flush();
//       ctx.close();
    }

    /**
     * 功能描述: 服务端给客户端发送消息
     * @Author yb
     * @Date 2023/5/8
     * @param  channelId 连接通道唯一id
     * @param  msg 需要发送的消息内容
     * @return void
     */
    public void channelWrite(ChannelId channelId, Object msg) throws Exception {
        Channel channel = ChannelMap.getChannelMap().get(channelId);
        if (channel == null) {
            log.info("通道:{},不存在",channelId);
            return;
        }
        if (msg == null || msg == "") {
            log.info("服务端响应空的消息");
            return;
        }
        //将客户端的信息直接返回写入ctx
        channel.write(msg);
        //刷新缓存区
        channel.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String socketString = ctx.channel().remoteAddress().toString();
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Client:{},READER_IDLE 读超时",socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                ChannelMap.removeChannelByName(id);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Client:{}, WRITER_IDLE 写超时",socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                ChannelMap.removeChannelByName(id);
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("Client:{},ALL_IDLE 总超时",socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                ChannelMap.removeChannelByName(id);
            }
        }
    }

    /**
     * 功能描述: 发生异常会触发此函数
     * @Author yb
     * @Date 2023/5/8
     * @param  ctx 通道处理程序上下文
     * @param  cause 异常
     * @return void
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.info("{}:发生了错误,此连接被关闭。此时连通数量:{}",ctx.channel().id(),ChannelMap.getChannelMap().size());
    }

}

