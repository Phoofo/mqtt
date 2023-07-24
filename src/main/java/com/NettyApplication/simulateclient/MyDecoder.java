package com.NettyApplication.simulateclient;

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
//public class MyDecoder extends ByteToMessageDecoder {
//    @Override
//    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
//        //创建字节数组,buffer.readableBytes可读字节长度
//        byte[] b = new byte[byteBuf.readableBytes()];
//        //复制内容到字节数组b
//        byteBuf.readBytes(b);
//        //字节数组转字符串
//        String str = new String(b);
//
//        list.add(bytesToHexString(b));
//    }
//
//    public String bytesToHexString(byte[] bArray) {
//        StringBuffer sb = new StringBuffer(bArray.length);
//        String sTemp;
//        for (int i = 0; i < bArray.length; i++) {
//            sTemp = Integer.toHexString(0xFF & bArray[i]);
//            if (sTemp.length() < 2) {
//                sb.append(0);
//            }
//            sb.append(sTemp.toUpperCase());
//        }
//        return sb.toString();
//    }
//
//    public static String toHexString1(byte[] b) {
//        StringBuffer buffer = new StringBuffer();
//        for (int i = 0; i < b.length; ++i) {
//            buffer.append(toHexString1(b[i]));
//        }
//        return buffer.toString();
//    }
//
//    public static String toHexString1(byte b) {
//        String s = Integer.toHexString(b & 0xFF);
//        if (s.length() == 1) {
//            return "0" + s;
//        } else {
//            return s;
//        }
//    }
//
//}
public class MyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < 8) {
            return;
        }
        //我们标记一下当前的readIndex的位置
        byteBuf.markReaderIndex();
        // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        int dataLength = 8;

        //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }else if(byteBuf.readableBytes() == 9){
            byteBuf.markReaderIndex();
            byte[] data = new byte[9];
            byteBuf.readBytes(data);
            String msg = bytesToHexString(data);
            out.add(msg);
        }else {
            byte[] data = new byte[dataLength];
            byteBuf.readBytes(data);
            String msg = bytesToHexString(data);
            out.add(msg);
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


