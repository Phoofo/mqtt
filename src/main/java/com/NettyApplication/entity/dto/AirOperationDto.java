package com.NettyApplication.entity.dto;

import lombok.Data;

@Data
public class AirOperationDto {

    /**
     * 主板编号
     */
    private Short controlId;

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 设备类型ID
     */
    private Long deviceTypeId;

    /**
     * 空调操作
     * //01查询
     * //02开机(自动)
     * //03关机
     * //04制冷
     * //05制热
     * //06除湿
     */
    private String operation;

}
