package com.NettyApplication.controller;


import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.setting.MapBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * <p>
 * 设备信息模块
 * </p>
 *
 * @author p
 * @since 2023-07-25
 */
@RestController
@CrossOrigin
@RequestMapping("/DeviceInfo")
@Tag(name = "DeviceInfo", description = "设备信息")
public class DeviceInfoController {

    @Resource
    IDeviceInfoService iDeviceInfoService;

    @Operation(description = "设备信息列表")
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(MapBuilder.builder()
                .put("control", iDeviceInfoService.list()
                        .stream().map(DeviceInfo::getControlId).distinct().collect(Collectors.toList()))
                .put("data", iDeviceInfoService.list())
                .build());
    }
}
