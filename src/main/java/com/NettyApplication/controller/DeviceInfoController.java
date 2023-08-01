package com.NettyApplication.controller;


import com.NettyApplication.entity.Control;
import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.service.IControlService;
import com.NettyApplication.service.IDeviceInfoService;
import com.NettyApplication.setting.MapBuilder;
import com.NettyApplication.tool.MessageProducer;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
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
    @Resource
    IControlService controlService;
    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource
    MessageProducer messageProducer;

    @Operation(description = "设备信息列表")
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(MapBuilder.builder()
                .put("control", iDeviceInfoService.list()
                        .stream().map(DeviceInfo::getControlId).distinct().collect(Collectors.toList()))
                .put("data", iDeviceInfoService.list())
                .build());
    }

    @Operation(description = "主控板列表")
    @GetMapping("/listControl")
    public ResponseEntity<?> listControl() {
        return ResponseEntity.ok(MapBuilder.builder()
                .put("data", controlService.list())
                .build());
    }

    @Operation(description = "主控板下设备信息列表")
    @GetMapping("/listByControl")
    public ResponseEntity<?> listControl(Short controlId) {
        return ResponseEntity.ok(MapBuilder.builder()
                .put("data", iDeviceInfoService.list(Wrappers.lambdaQuery(DeviceInfo.class)
                        .eq(DeviceInfo::getControlId, controlId)))
                .build());
    }

    @Operation(description = "配置主控板位置")
    @PostMapping("/configurationLocation")
    public ResponseEntity<?> configurationLocation(@RequestBody Control control) {
        controlService.configurationLocation(control);
        return ResponseEntity.ok(MapBuilder.builder()
                .build());
    }

}
