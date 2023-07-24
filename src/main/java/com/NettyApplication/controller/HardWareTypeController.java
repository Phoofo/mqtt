package com.NettyApplication.controller;


import cn.hutool.core.util.ObjectUtil;
import com.NettyApplication.entity.HardWareType;
import com.NettyApplication.service.IHardWareTypeService;
import com.NettyApplication.setting.MapBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@RestController
@RequestMapping("/NettyApplication/hard-ware-type")
@Tag(name = "hardWareType",description = "类型设置")
public class HardWareTypeController {
    @Resource
    private  IHardWareTypeService iHardWareTypeService;

    @Operation(description = "设置硬件类型")
    @PostMapping("/set")
    public ResponseEntity<?> setHardWareType(@RequestBody HardWareType form) {
        boolean save = iHardWareTypeService.save(form);
        return ResponseEntity.ok(MapBuilder.builder()
                .put("msg", "成功")
                .build());
    }
    @Operation(description = "查询硬件类型")
    @GetMapping("/get")
    public ResponseEntity<?> setHardWareType() {
        List<HardWareType> list = iHardWareTypeService.list();
        return ResponseEntity.ok(MapBuilder.builder()
                .put("msg", list)
                .build());
    }


}
