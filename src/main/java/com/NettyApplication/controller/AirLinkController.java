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
//    @Parameter(name = "id",description = "id",in = ParameterIn.HEADER,example = "1")
//    @Parameter(name = "name",description = "name",in = ParameterIn.HEADER,example = "jack")
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


   /* @Operation(description = "获取硬件状态")
//    @Parameter(name = "id",description = "id",in = ParameterIn.HEADER,example = "1")
//    @Parameter(name = "name",description = "name",in = ParameterIn.HEADER,example = "jack")
    @GetMapping("/get")
    public ResponseEntity<String> getInfo(@RequestBody HardWareModel hardWare){
        //处理发送的指令
//        byte[] msgBytes = {(byte) 0xAA, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,0xFF};
        System.out.println(hardWare);
        byte[] msgBytes = {
//                Byte.parseByte(hardWare.getHeader().substring(2), 16),
                (byte) Integer.parseInt(hardWare.getHeader(), 16),
                (byte) Integer.parseInt(hardWare.getAddress(), 16),
                (byte) Integer.parseInt(hardWare.getFunction1(), 16),
                (byte) Integer.parseInt(hardWare.getFunction2(), 16),
                (byte) Integer.parseInt(hardWare.getFunction3(), 16),
                (byte) Integer.parseInt(hardWare.getFunction4(), 16),
                (byte) Integer.parseInt(hardWare.getEnd(), 16),
        };
        dtuManage.sendMsg(msgBytes);
        hardWare.getHeader().getBytes();
        return ResponseEntity.ok("Success!");
    }*/
}
