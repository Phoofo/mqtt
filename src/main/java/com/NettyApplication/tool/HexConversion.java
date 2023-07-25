package com.NettyApplication.tool;

import com.NettyApplication.toolmodel.DatagramEntity;

public class HexConversion {

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            String hexString = String.format("%02X", b & 0xFF);
            sb.append(hexString).append(" ");
        }
        return sb.toString().trim();
    }

    // 静态方法，将字节数组转换为 DatagramEntity 对象
    public static DatagramEntity ByteArrayToDatagramEntity(byte[] byteArray) {
        String[] hexStringArray = new String[byteArray.length];

        for (int i = 0; i < byteArray.length; i++) {
            String hexString = String.format("%02X", byteArray[i] & 0xFF);
            hexStringArray[i] = hexString;
        }

        // 解析字节数组并构造 DataEntity 对象
        String header = hexStringArray[0];
        String type = hexStringArray[1];
        String deviceNumber = hexStringArray[2];
        String function1 = hexStringArray[3];
        String function2 = hexStringArray[4];
        String function3 = hexStringArray[5];
        String function4 = hexStringArray[6];
        String ending = hexStringArray[7];

        return new DatagramEntity(header, type, deviceNumber, function1, function2, function3, function4, ending);
    }
}
