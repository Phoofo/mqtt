package com.NettyApplication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/annotation")
@Tag(name = "annotation",description = "控制层注解测试")
public class Test {

    @Operation(summary = "Operation注解测试",description = "Operation注解测试",operationId = "getInfo")
    @ApiResponse(responseCode = "200",description = "请求成功!")
    @ApiResponse(responseCode = "400",description = "请求异常")
    @Parameter(name = "id",description = "id",in = ParameterIn.HEADER,example = "1")
    @Parameter(name = "name",description = "name",in = ParameterIn.HEADER,example = "jack")
    @GetMapping("/get")
    public ResponseEntity<String> getInfo(
//            @RequestParam(name = "id",value = "id") Integer id,
//            @RequestParam(name = "name",value = "name") String name
    ){
        return ResponseEntity.ok("Success!");
    }
}
