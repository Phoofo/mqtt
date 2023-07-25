package com.NettyApplication.toolmodel;

import lombok.Data;

@Data
public class EightByteEntity {
    private byte header;
    private byte type;
    private byte address;
    private byte status1;
    private byte status2;
    private byte status3;
    private byte status4;
    private byte ending;


}
