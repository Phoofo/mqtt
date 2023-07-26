package com.NettyApplication.controller;

import com.NettyApplication.controllermodel.HardWareModel;
import com.NettyApplication.entity.HardWare;
import com.NettyApplication.listen.DtuManage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/airLink")
@Tag(name = "airLink", description = "空调")
@CrossOrigin
public class AirLinkController {
    @Resource
    private DtuManage dtuManage;

    @Operation(description = "设置空调温度/关机/开机/查询....")
    @GetMapping("/set")
    public ResponseEntity<String> set() {

        byte[] msgBytes = {
                (byte) Integer.parseInt("AA", 16),
                (byte) Integer.parseInt("01", 16),
                (byte) Integer.parseInt("04", 16),
                (byte) Integer.parseInt("01", 16),
                (byte) Integer.parseInt("00", 16),
                (byte) Integer.parseInt("00", 16),
                (byte) Integer.parseInt("00", 16),
                (byte) Integer.parseInt("FE", 16)
        };
        //01查询
        //02开机(自动)
        //03关机
        //04制冷
        //05制热
        //06除湿
        short s = 01;
        //设置硬件的状态
        dtuManage.sendMsg(msgBytes, s);

        return ResponseEntity.ok("Success!");
    }
}
