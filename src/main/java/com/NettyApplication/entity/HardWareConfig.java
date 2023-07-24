package com.NettyApplication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("t_hard_ware_config")
public class HardWareConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 硬件配置，记录每个字段的含义
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String type;

    private String header;

    private String address;

    private String function1;

    private String function2;

    private String function3;

    private String function4;

    private String end;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getFunction1() {
        return function1;
    }

    public void setFunction1(String function1) {
        this.function1 = function1;
    }
    public String getFunction2() {
        return function2;
    }

    public void setFunction2(String function2) {
        this.function2 = function2;
    }
    public String getFunction3() {
        return function3;
    }

    public void setFunction3(String function3) {
        this.function3 = function3;
    }
    public String getFunction4() {
        return function4;
    }

    public void setFunction4(String function4) {
        this.function4 = function4;
    }
    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "HardWareConfig{" +
            "id=" + id +
            ", type=" + type +
            ", header=" + header +
            ", address=" + address +
            ", function1=" + function1 +
            ", function2=" + function2 +
            ", function3=" + function3 +
            ", function4=" + function4 +
            ", end=" + end +
        "}";
    }
}
