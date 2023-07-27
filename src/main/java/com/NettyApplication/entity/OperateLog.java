package com.NettyApplication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作报文记录
 */
@TableName("t_operate_log")
@Data
public class OperateLog implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键id*
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 主板编号
     */
    private Short controlId;

    /**
     * 设备编号
     */
    private Byte deviceId;

    /**
     * 设备类型ID
     */
    private Byte deviceTypeId;

    /**
     * 是否回写
     */
    private Boolean writeBack;

    /**
     * 创建时间*
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdDate;

    /**
     * 修改时间*
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastModifiedDate;
}
