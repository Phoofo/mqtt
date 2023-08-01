package com.NettyApplication.toolmodel;

import lombok.Data;

import java.io.Serializable;

@Data
public class RedisMessage implements Serializable {
    private short controlId;//主板
    private byte type;//设备类型
    private byte deviceId;//设备
    private byte operation;//操作指令
    private String key;//redis-key
    private byte[] msgBytes;//操作报文
}
