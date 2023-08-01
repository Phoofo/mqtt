package com.NettyApplication.tool;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class MessageProducer {

    @Resource
    RedisTemplate<String, Object> redisTemplate;
    private final long messageTimeout = 2000; // 消息超时时间，单位: 毫秒
    private final String lockKey = "messageLock"; // 分布式锁的键名
    private final String destinationQueue = "fail"; // 首次无响应队列

    public void sendMessage(String sourceQueue, Object message) {
        redisTemplate.opsForList().rightPush(sourceQueue, message);
//        redisTemplate.expire(sourceQueue, messageTimeout, TimeUnit.MILLISECONDS);
        new Thread(() -> {
            // 使用分布式锁确保只有一个线程可以执行超时处理逻辑
            boolean lockAcquired = false;
            try {
//                lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", messageTimeout, TimeUnit.MILLISECONDS);
//                if (lockAcquired) {
                    // 等待2秒
                    System.out.println(LocalDateTime.now());
                    Thread.sleep(messageTimeout);
                    System.out.println(LocalDateTime.now());
                    // 检查消息是否超时未被消费
                    Object poppedMessage = redisTemplate.opsForList().leftPop(sourceQueue);
                    System.out.println("message:" + message);
                    System.out.println("poppedMessage:" + poppedMessage);
                    if (poppedMessage != null && message.equals(poppedMessage)) {
                        // 将超时未消费的消息转移到另一个队列
                        redisTemplate.opsForList().rightPush(destinationQueue, poppedMessage);
                    }
//                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (lockAcquired) {
                    // 释放分布式锁
                    redisTemplate.delete(lockKey);
                }
            }
        }).start();
    }
}
