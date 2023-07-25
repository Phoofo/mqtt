package com.NettyApplication.toolmodel;

import lombok.Data;

@Data
public class TenByteEntity {
    private short mainboardAddress;
    private byte airConditionerCount;
    private byte doorLockCount;
    private byte curtainCount;
    private byte window;
    private byte spare1;
    private byte spare2;
    private byte spare3;
    private byte spare4;

}