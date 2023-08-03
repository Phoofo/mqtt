package com.NettyApplication.listen;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.NettyApplication.entity.Control;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.service.IControlService;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.tool.MessageProducer;
import com.NettyApplication.toolmodel.EightByteEntity;
import com.NettyApplication.toolmodel.RedisMessage;
import com.NettyApplication.toolmodel.TenByteEntity;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        /**
         * 获取IP和端口号
         * InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
         * String clientIp = insocket.getAddress().getHostAddress();
         * int clientPort = insocket.getPort();
         * */

        //获取连接通道唯一标识
        ChannelId channelId = ctx.channel().id();
        //如果map中不包含此连接，就保存连接
        if (ChannelMap.getChannelMap().containsKey(channelId)) {
            log.info("硬件:{},已经是连接状态，，连接通道数量:{} ", channelId, ChannelMap.getChannelMap().size());
        } else {
            //才激活  保存连接
            ConcurrentHashMap<String, Object> objectObjectConcurrentHashMap = new ConcurrentHashMap<>();
            objectObjectConcurrentHashMap.put("channel", ctx.channel());
            ChannelMap.addChannelOrTenByteEntity(channelId, objectObjectConcurrentHashMap);
            log.info("新的硬件：{channelId}才建立链接,channelActive连接通道数量: {}", channelId, ChannelMap.getChannelDetail().size());
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
        ChannelId channelId = ctx.channel().id();

        //todo 鲜帅处理数据库主板的状态

        //包含此客户端才去删除
        if (ChannelMap.getChannelDetail().containsKey(channelId)) {
            //删除连接
            ChannelMap.getChannelDetail().remove(channelId);
            log.info("硬件：{}，终止连接服务器，目前连接通道数量:{} ", channelId, ChannelMap.getChannelMap().size());
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

        if (msg instanceof TenByteEntity) {
            //如果是10个字节的心跳包数据，解析心跳包，保存主控板的信息和硬件的信息
            TenByteEntity entity = (TenByteEntity) msg;
            log.info("【硬件，注册报文或者心跳包】客户端id:{},客户端消息:{}" + ctx.channel().id(), entity.toString());
            //保存主板地址到map做临时缓存
            ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
            ConcurrentHashMap<String, Object> stringObjectConcurrentHashMap = channelDetail.get(ctx.channel().id());
            //map里面不存在主机地址
            if (!stringObjectConcurrentHashMap.containsKey("address")) {
                stringObjectConcurrentHashMap.put("address", entity.getMainboardAddress());
            }

            //注册主板信息不为空
            if (ObjectUtil.isNotNull(entity.getMainboardAddress())) {
                /**
                 * 处理主板
                 * */
                IControlService controlService = context.getBean(IControlService.class);
                Control one = controlService.getById(entity.getMainboardAddress());
                if (ObjectUtil.isNotNull(one)) {
                    // 更新/新增主板信息
                    one.setConnectionStatus(true);
                    one.setLastModifiedDate(LocalDateTime.now());
                    controlService.updateById(one);
                } else {
                    //添加主板信息
                    Control control = new Control();
                    control.setId(entity.getMainboardAddress());
                    control.setConnectionStatus(true);
                    control.setCreatedDate(LocalDateTime.now());
                    controlService.save(control);
                }
                /**
                 * 处理主板关联设备
                 * */
                IDeviceInfoService deviceInfoService = context.getBean(IDeviceInfoService.class);
                if (ObjectUtil.isNotNull(entity.getAirConditionerCount())) {//空调信息不为空
                    long count = deviceInfoService.count(Wrappers.lambdaQuery(DeviceInfo.class)
                            .eq(DeviceInfo::getControlId, entity.getMainboardAddress())
                            //todo 先写死为空调
                            .eq(DeviceInfo::getDeviceTypeId, 1L));
                    //此次注册设备与上次不一致，重新添加此次注册设备
                    if ((byte) count != entity.getAirConditionerCount()) {
                        //清除之前保存的设备
                        List<DeviceInfo> list = deviceInfoService.list(Wrappers.lambdaQuery(DeviceInfo.class)
                                .eq(DeviceInfo::getControlId, entity.getMainboardAddress())
                                .eq(DeviceInfo::getDeviceTypeId, 1L));
                        //list批量循环删除
                        list.forEach(deviceInfoService::removeById);
                        //添加此次注册设备
                        for (byte i = 1; i <= entity.getAirConditionerCount(); i++) {
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setControlId(entity.getMainboardAddress());
                            deviceInfo.setDeviceId(i);
                            deviceInfo.setDeviceTypeId((byte) 1);
                            //todo 设备默认是没有链接的
                            deviceInfo.setIsConnect(Boolean.FALSE);
                            deviceInfoService.save(deviceInfo);
                        }
                    }
                }
            }
        } else if (msg instanceof EightByteEntity) {
            //如果是8个字节数据硬件状态，这是硬件返回的信息
            EightByteEntity entity = (EightByteEntity) msg;
            log.info("【硬件，数据包】有客户端发送消息,客户端id:{},客户端消息:{}", ctx.channel().id(), entity.toString());
            if (ObjectUtil.isNotNull(entity)) {
                ConcurrentHashMap<ChannelId, ConcurrentHashMap<String, Object>> channelDetail = ChannelMap.getChannelDetail();
                if (CollectionUtils.isEmpty(channelDetail)) {
                    //可能主板全部断开了链接
                    log.info("【硬件，数据包】发送了消息，但是map缓存是空的******");
                    return;
                }
                //获取设备硬件通信信息
                Map<String, Object> channelInfo = channelDetail.get(ctx.channel().id());
                //主板字节获取
                Short controlId = null;
                if (channelInfo != null) {
                    controlId = (Short) channelInfo.get("address");
                    IDeviceInfoService deviceInfoService = context.getBean(IDeviceInfoService.class);
                    DeviceInfo one = deviceInfoService.getOne(Wrappers.lambdaQuery(DeviceInfo.class)
                            .eq(DeviceInfo::getControlId, controlId)//主板编码
                            .eq(DeviceInfo::getDeviceId, entity.getAddress())//设备编码
                            .eq(DeviceInfo::getDeviceTypeId, 1L)//设备类型
                    );
                    //更新设备信息
                    if (ObjectUtil.isNotNull(one)) {
                        //更新数据库硬件的状态
                        one.setStateA(entity.getStatus1());
                        one.setStateB(entity.getStatus2());
                        one.setStateC(entity.getStatus3());
                        one.setStateD(entity.getStatus4());
                        one.setIsConnect(Boolean.TRUE);
                        one.setLastModifiedDate(LocalDateTime.now());
                        deviceInfoService.updateById(one);
                    }
                    // 处理redis的数据 删除与该指令相关的set，hash，list的数据
                    String key = controlId + ":" + one.getDeviceTypeId() + ":" + one.getDeviceId();
                    RedisTemplate redisTemplate = context.getBean(RedisTemplate.class);
                    //移除相关set
                    SetOperations<String, Object> stringObjectSetOperations = redisTemplate.opsForSet();
                    stringObjectSetOperations.remove("id", key);
                    //移除相关hash,移之前取出对应此次报文的操作
                    HashOperations<String, Object, Object> stringObjectObjectHashOperations = redisTemplate.opsForHash();
                    Byte operation = (Byte) stringObjectObjectHashOperations.get(key, "operation");
                    stringObjectObjectHashOperations.delete(key);
                    //消费相关list主板队列
                    ListOperations listOperations = redisTemplate.opsForList();
                    //获取该设备主板队列的所有元素
                    List<String> listValues = listOperations.range(controlId, 0, -1);
                    int size = listValues.size();
                    if (size != 0) {
                        //list里面有主板信息
                        boolean containsValue = listValues.contains(key);
                        //消费
                        listOperations.leftPop(controlId);
                        //查看操作是否是查询
                        byte select = (byte) Integer.parseInt("01", 16);
                        if (select != operation) {
                            // 指令报文封装
                            byte[] msgBytes = {
                                    (byte) Integer.parseInt("AA", 16),//开头
                                    (byte) Integer.parseInt("01", 16),//设备类型:空调01
                                    one.getDeviceId(),//设备编号
                                    select,//功能:01查询;02开机(自动);03关机;04制冷;05制热;06除湿
                                    (byte) Integer.parseInt("00", 16),//可拓展参数
                                    (byte) Integer.parseInt("00", 16),//可拓展参数
                                    (byte) Integer.parseInt("00", 16),//可拓展参数
                                    (byte) Integer.parseInt("FE", 16) //结尾
                            };
                            //处理redis
                            stringObjectSetOperations.add("id", key);//保存set信息
                            listOperations.leftPush(controlId.toString(), key);
                            stringObjectObjectHashOperations.put(key, "operation", select);
                            stringObjectObjectHashOperations.put(key, "number", 3);//已发送记录2，未发生记录3
                            stringObjectObjectHashOperations.put(key, "time", LocalDateTime.now());
                            stringObjectObjectHashOperations.put(key, "message", msgBytes);
                            //发送查询指令
                            DeviceServe deviceServe = context.getBean(DeviceServe.class);
                            deviceServe.sendMsg(msgBytes, controlId, one.getDeviceId(), select, one.getDeviceTypeId());
                        } else {

                        }

//                        if (containsValue) {
//                            Long index = null;
//                            for (long i = 0; i < size; i++) {
//                                String element = (String) listOperations.index(controlId, i);
//                                if (key.equals(element)) {
//                                    index = i;
//                                    break;
//                                }
//                            }
//                            if (index == 0) {
//                                //消费
//                                listOperations.leftPop(controlId);
//                                //set删除
//                                stringObjectSetOperations.remove("id", key);
//                                //hash里面删除
//                                stringObjectObjectHashOperations.delete("id", key);
//
//
//                            } else {
//                                //todo 报文提交到达，怎么处理，删除前面的说有数据，更改数据库状态
//                            }
//                        }

                    }
                }
                log.info("【硬件，数据包】发送了消息，没有channelid对应的数据******");
            }
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

