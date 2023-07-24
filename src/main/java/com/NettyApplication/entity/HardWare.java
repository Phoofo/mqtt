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
@TableName("t_hard_ware")
public class HardWare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 硬件明细
     */
    private Integer id;

    /**
     * 控制板ID
     */
    private Integer controlId;

    private String state;

    /**
     * 主控板控制的硬件编号
     */
    private String number;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getControlId() {
        return controlId;
    }

    public void setControlId(Integer controlId) {
        this.controlId = controlId;
    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "HardWare{" +
            "id=" + id +
            ", controlId=" + controlId +
            ", state=" + state +
            ", number=" + number +
        "}";
    }
}
