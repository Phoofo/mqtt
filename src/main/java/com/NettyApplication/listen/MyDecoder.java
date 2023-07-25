package com.NettyApplication.listen;

import com.NettyApplication.toolmodel.EightByteEntity;
import com.NettyApplication.toolmodel.TenByteEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 功能描述: 自定义接收消息格式
 *
 * @Author yb
 * @Date 2023/05/08
 */
public class MyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 根据字节流长度判断是 10 个字节还是 8 个字节
        if (byteBuf.readableBytes() == 10) {
            TenByteEntity entity = new TenByteEntity();
            entity.setMainboardAddress(byteBuf.readShort());
            entity.setAirConditionerCount(byteBuf.readByte());
            entity.setDoorLockCount(byteBuf.readByte());
            entity.setCurtainCount(byteBuf.readByte());
            entity.setWindow(byteBuf.readByte());
            entity.setSpare1(byteBuf.readByte());
            entity.setSpare2(byteBuf.readByte());
            entity.setSpare3(byteBuf.readByte());
            entity.setSpare4(byteBuf.readByte());
            out.add(entity);
        } else if (byteBuf.readableBytes() == 8) {
            EightByteEntity entity = new EightByteEntity();
            entity.setHeader(byteBuf.readByte());
            entity.setType(byteBuf.readByte());
            entity.setAddress(byteBuf.readByte());
            entity.setStatus1(byteBuf.readByte());
            entity.setStatus2(byteBuf.readByte());
            entity.setStatus3(byteBuf.readByte());
            entity.setStatus4(byteBuf.readByte());
            entity.setEnding(byteBuf.readByte());

            out.add(entity);
        } else {
            // 不符合预期的字节长度，忽略该消息
            byteBuf.skipBytes(byteBuf.readableBytes());
        }
    }

    public String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String toHexString1(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; ++i) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

}


