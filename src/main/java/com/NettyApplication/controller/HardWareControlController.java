package com.NettyApplication.controller;


import com.NettyApplication.entity.HardWareControl;
import com.NettyApplication.service.IHardWareControlService;
import com.NettyApplication.setting.MapBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/NettyApplication/hard-ware-control")
@Tag(name = "HardWareControl",description = "主控版设置")
public class HardWareControlController {
    @Resource
    private IHardWareControlService iHardWareControlService;

    @Operation(description = "添加主控版")
    @PostMapping("/set")
    public ResponseEntity<?> setHardWareType(@RequestBody HardWareControl form) {
        boolean save = iHardWareControlService.save(form);
        return ResponseEntity.ok(MapBuilder.builder()
                .put("msg", "成功")
                .build());
    }
    @Operation(description = "查询主控板列表")
    @GetMapping("/get")
    public ResponseEntity<?> setHardWareType() {
        List<HardWareControl> list = iHardWareControlService.list();
        return ResponseEntity.ok(MapBuilder.builder()
                .put("msg", list)
                .build());
    }

}
