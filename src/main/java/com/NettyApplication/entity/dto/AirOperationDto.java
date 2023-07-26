package com.NettyApplication.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class AirOperationDto {

    /**
     * 主板编号
     */
    private short controlId;

    /**
     * 设备编号集合
     */
    private List<Byte> deviceIds;

    /**
     * 设备编号
     */
    private byte deviceId;

    /**
     * 设备类型ID
     */
    private byte deviceTypeId;

    /**
     * 操作方式 ：1,批量选择;2,主板一键
     */
    private Integer operationType;

    /**
     * 空调操作
     * //01查询
     * //02开机(自动)
     * //03关机
     * //04制冷
     * //05制热
     * //06除湿
     */
    private byte operation;

}
