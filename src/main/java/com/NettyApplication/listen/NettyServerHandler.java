package com.NettyApplication.listen;

import cn.hutool.core.util.ObjectUtil;
import com.NettyApplication.entity.Control;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.entity.OperateLog;
import com.NettyApplication.service.IControlService;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.service.IOperateLogService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
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
     *
     * @Author yb
     * @Date 2023/5/8
     * @param ctx 通道
     * @return void
     */
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().write(new byte[]{0x00});
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
            log.info("客户端:{},是连接状态，连接通道数量:{} ", channelId, ChannelMap.getChannelMap().size());
        } else {
            log.info(ctx.toString());
            log.info(ctx.channel().toString());
            //才激活  保存连接
            ConcurrentHashMap<String, Object> objectObjectConcurrentHashMap = new ConcurrentHashMap<>();
            objectObjectConcurrentHashMap.put("channel", ctx.channel());
            ChannelMap.addChannelOrTenByteEntity(channelId, objectObjectConcurrentHashMap);
            log.info("客户端:{},连接netty服务器[IP:{}-->PORT:{}]", channelId, clientIp, clientPort);
            log.info("channelActive连接通道数量: {}", ChannelMap.getChannelDetail().size());
        }
    }

    /**
     * 功能描述: 有客户端终止连接服务器会触发此函数
     *
     * @param ctx 通道处理程序上下文
     * @return void
     * @Author yb
     * @Date 2023/5/8
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        ChannelId channelId = ctx.channel().id();
        log.info("断开1客户端:{},连接netty服务器[IP:{}-->PORT:{}]", channelId, clientIp, inSocket.getPort());
        //包含此客户端才去删除
        if (ChannelMap.getChannelDetail().containsKey(channelId)) {
            //删除连接
            ChannelMap.getChannelDetail().remove(channelId);
            log.info("断开2客户端:{},连接netty服务器[IP:{}-->PORT:{}]", channelId, clientIp, inSocket.getPort());
            log.info("channelInactive连接通道数量: " + ChannelMap.getChannelMap().size());
        }
    }

    /**
     * 功能描述: 有客户端发消息会触发此函数
     *
     * @param ctx 通道处理程序上下文
     * @param msg 客户端发送的消息
     * @return void
     * @Author yb
     * @Date 2023/5/8
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("加载客户端报文,客户端id:{},客户端消息:{}", ctx.channel().id(), msg);

        if (msg instanceof TenByteEntity) {
            //如果是10个字节的心跳包数据，解析心跳包，保存主控板的信息和硬件的信息
            TenByteEntity entity = (TenByteEntity) msg;
            log.info(entity.toString());
            //保存主机地址
            ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
            ConcurrentHashMap<String, Object> stringObjectConcurrentHashMap = channelDetail.get(ctx.channel().id());
            stringObjectConcurrentHashMap.put("address", entity.getMainboardAddress());
            //注册主板信息不为空
            if (ObjectUtil.isNotNull(entity.getMainboardAddress())) {
                // 更新/新增主板信息
                IControlService controlService = context.getBean(IControlService.class);
                Control one = controlService.getById(entity.getMainboardAddress());
                if (ObjectUtil.isNotNull(one)) {
                    one.setConnectionStatus(true);
                    one.setLastModifiedDate(LocalDateTime.now());
                    controlService.updateById(one);
                } else {
                    Control control = new Control();
                    control.setId(entity.getMainboardAddress());
                    control.setConnectionStatus(true);
                    control.setCreatedDate(LocalDateTime.now());
                    controlService.save(control);
                }
                IDeviceInfoService deviceInfoService = context.getBean(IDeviceInfoService.class);
                if (ObjectUtil.isNotNull(entity.getAirConditionerCount())) {//空调信息不为空
                    long count = deviceInfoService.count(Wrappers.lambdaQuery(DeviceInfo.class)
                            .eq(DeviceInfo::getControlId, entity.getMainboardAddress())
                            .eq(DeviceInfo::getDeviceTypeId, 1L));
                    //此次注册设备与上次不一致，重新添加此次注册设备
                    if ((byte) count != entity.getAirConditionerCount()) {
                        //清除之前保存的设备
                        List<DeviceInfo> list = deviceInfoService.list(Wrappers.lambdaQuery(DeviceInfo.class)
                                .eq(DeviceInfo::getControlId, entity.getMainboardAddress())
                                .eq(DeviceInfo::getDeviceTypeId, 1L));
                        list.forEach(deviceInfoService::removeById);
                        //添加此次注册设备
                        for (byte i = 1; i <= entity.getAirConditionerCount(); i++) {
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setControlId(entity.getMainboardAddress());
                            deviceInfo.setDeviceId(i);
                            deviceInfo.setDeviceTypeId((byte) 1);
                            deviceInfo.setIsConnect(Boolean.TRUE);
                            deviceInfoService.save(deviceInfo);
                        }
                    }
                }
            }
        } else if (msg instanceof EightByteEntity) {
            //如果是8个字节数据硬件状态，这是硬件返回的信息
            EightByteEntity entity = (EightByteEntity) msg;
            log.info(entity.toString());
            if (ObjectUtil.isNotNull(entity)) {
                ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
                if (CollectionUtils.isEmpty(channelDetail)) {
                    return;
                }
                //获取设备信息
                Short address = (Short) channelDetail.get(ctx.channel().id()).get("address");
                IDeviceInfoService deviceInfoService = context.getBean(IDeviceInfoService.class);
                DeviceInfo one = deviceInfoService.getOne(Wrappers.lambdaQuery(DeviceInfo.class)
                        .eq(DeviceInfo::getControlId, address)//主板编码
                        .eq(DeviceInfo::getDeviceId, entity.getAddress())//设备编码
                        .eq(DeviceInfo::getDeviceTypeId, 1L)//设备类型
                );
                //更新设备信息
                if (ObjectUtil.isNotNull(one)) {
                    one.setStateA(entity.getStatus1());
                    one.setStateB(entity.getStatus2());
                    one.setStateC(entity.getStatus3());
                    one.setStateD(entity.getStatus4());
                    one.setIsConnect(Boolean.TRUE);
                    one.setLastModifiedDate(LocalDateTime.now());
                    deviceInfoService.updateById(one);
                    //回写操作日志确保操作指令有回应
                    setBack(entity, address);
                }
            }
        }
        // 将响应写回给客户端
        // ctx.writeAndFlush(response);
    }

    @Transactional
    void setBack(EightByteEntity entity, Short address) {
        IOperateLogService operateLogService = context.getBean(IOperateLogService.class);
        List<OperateLog> list = operateLogService.list(Wrappers.lambdaQuery(OperateLog.class)
                .eq(OperateLog::getControlId, address)
                .eq(OperateLog::getDeviceId, entity.getAddress())
                .eq(OperateLog::getDeviceTypeId, entity.getType())
                .eq(OperateLog::getWriteBack, false));
        if (list.size() > 0) {
            list.forEach(o -> {
                o.setLastModifiedDate(LocalDateTime.now());
                o.setWriteBack(true);
                operateLogService.updateById(o);
            });
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 刷新数据并关闭连接
        ctx.flush();
//       ctx.close();
    }

    /**
     * 功能描述: 服务端给客户端发送消息
     *
     * @param channelId 连接通道唯一id
     * @param msg       需要发送的消息内容
     * @return void
     * @Author yb
     * @Date 2023/5/8
     */
    public void channelWrite(ChannelId channelId, Object msg) throws Exception {
        Channel channel = ChannelMap.getChannelMap().get(channelId);
        if (channel == null) {
            log.info("通道:{},不存在", channelId);
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
                log.info("Client:{},READER_IDLE 读超时", socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                connectionDisconnected(ctx);
                ChannelMap.removeDetailChannelByName(id);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Client:{}, WRITER_IDLE 写超时", socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                connectionDisconnected(ctx);
                ChannelMap.removeDetailChannelByName(id);
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("Client:{},ALL_IDLE 总超时", socketString);
                ctx.disconnect();
                Channel channel = ctx.channel();
                ChannelId id = channel.id();
                connectionDisconnected(ctx);
                ChannelMap.removeDetailChannelByName(id);
            }
        }
    }

    /**
     * 主控板连接状态断开
     *
     * @param ctx
     * @return
     */
    private void connectionDisconnected(ChannelHandlerContext ctx) {
        IControlService controlService = context.getBean(IControlService.class);
        ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
        if (CollectionUtils.isEmpty(channelDetail)) {
            return;
        }
        //获取设备信息
        Short address = (Short) channelDetail.get(ctx.channel().id()).get("address");
        Control one = controlService.getById(address);
        if (ObjectUtil.isNotNull(one)) {
            one.setConnectionStatus(false);
            controlService.updateById(one);
        }
    }

    /**
     * 功能描述: 发生异常会触发此函数
     *
     * @param ctx   通道处理程序上下文
     * @param cause 异常
     * @return void
     * @Author yb
     * @Date 2023/5/8
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.info("{}:发生了错误,此连接被关闭。此时连通数量:{}", ctx.channel().id(), ChannelMap.getChannelMap().size());
    }

}

