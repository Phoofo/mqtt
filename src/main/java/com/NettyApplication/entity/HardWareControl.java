package com.NettyApplication.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@TableName("t_hard_ware_control")
public class HardWareControl implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所有硬件配置详情
     */
    private Integer id;

    /**
     * 地址，包括具体的位置和楼层
     */
    private String address;

    private Float longitude;

    private Float latitude;

    /**
     * t_hard_ware_type的id
     */
    private Integer typeId;

    private String ip;

    private String port;

    /**
     * 设备编号
     */
    private String number;

    private String state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }
    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "HardWareControl{" +
            "id=" + id +
            ", address=" + address +
            ", longitude=" + longitude +
            ", latitude=" + latitude +
            ", typeId=" + typeId +
            ", ip=" + ip +
            ", port=" + port +
            ", number=" + number +
            ", state=" + state +
        "}";
    }
}
