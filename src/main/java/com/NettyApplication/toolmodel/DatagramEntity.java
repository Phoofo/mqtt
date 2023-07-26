package com.NettyApplication.toolmodel;

import lombok.Data;

@Data
public class DatagramEntity {

    private String header;
    private String type;
    private String deviceNumber;
    private String function1;
    private String function2;
    private String function3;
    private String function4;
    private String ending;

    public DatagramEntity(){};
    public DatagramEntity(String header, String type, String deviceNumber, String function1, String function2, String function3, String function4, String ending) {
    }
}
