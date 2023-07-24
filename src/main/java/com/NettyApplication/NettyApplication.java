package com.NettyApplication;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.NettyApplication.mapper","com.NettyApplication.listen"})
public class NettyApplication{
    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
    }

}
