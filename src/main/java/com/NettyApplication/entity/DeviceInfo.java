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
 * 设备类型
 */
@TableName("t_device_info")
@Data
public class DeviceInfo implements Serializable {

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
    private Long deviceTypeId;

    /**
     * 状态A
     */
    private String stateA;

    /**
     * 状态B
     */
    private String stateB;

    /**
     * 状态C
     */
    private String stateC;

    /**
     * 状态D
     */
    private String stateD;

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
