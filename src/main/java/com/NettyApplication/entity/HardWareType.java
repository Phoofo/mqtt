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
@TableName("t_hard_ware_type")
public class HardWareType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 硬件类型
     */
    private String type;

    /**
     * 解释：什么空调，注意事项等
     */
    private String detail;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "HardWareType{" +
            "type=" + type +
            ", detail=" + detail +
            ", id=" + id +
        "}";
    }
}
