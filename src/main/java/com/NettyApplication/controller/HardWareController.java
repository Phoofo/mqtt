package com.NettyApplication.controller;


import com.NettyApplication.entity.HardWareControl;
import com.NettyApplication.service.IHardWareService;
import com.NettyApplication.setting.MapBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@RestController
@CrossOrigin
@RequestMapping("/NettyApplication/hard-ware")
@Tag(name = "HardWare",description = "硬件")
public class HardWareController {

    @Resource
    IHardWareService iHardWareService;
    @Operation(description = "硬件列表")
    @GetMapping("/get")
    public ResponseEntity<?> setHardWareType() {
        return ResponseEntity.ok(MapBuilder.builder()
                .put("msg", iHardWareService.selectActivitiesByUserId())
                .build());
    }
}
