package com.NettyApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootApplication
public class MyApplication {
    @Resource
    static RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
        contextLoads();
    }

    static void contextLoads() {
        redisTemplate.opsForList().leftPush("testKey", "L1");
        redisTemplate.opsForList().leftPush("testKey", "L2");
        redisTemplate.opsForList().leftPush("testKey", "L3");
        redisTemplate.opsForList().rightPush("testKey", "R4");
        redisTemplate.opsForList().rightPush("testKey", "R5");
        redisTemplate.opsForList().rightPush("testKey", "R6");
        redisTemplate.opsForList().rightPush("testKey", "R7");
        Object lValue = null;
        while (null != (lValue = redisTemplate.opsForList().leftPop("testKey"))) {
            System.out.println("leftPop" + lValue);
        }
    }
}
