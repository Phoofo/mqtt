package com.NettyApplication.tool;

import cn.hutool.core.util.ObjectUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class MessageProducer {

    @Resource
    RedisTemplate<String, Object> redisTemplate;
    @Resource
    HashOperations<String, String, String> hashOperations;
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


    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
//        new Thread(() -> {
//            try {
//                // 等待2秒
//                Thread.sleep(messageTimeout);
//                // 检查消息是否超时未被消费
//                Object poppedMessage = redisTemplate.opsForValue().get(key);
//                if (poppedMessage != null && value.equals(poppedMessage)) {
//                    // 将超时未消费的消息转移到另一个无响应队列
//                    redisTemplate.delete(key);
//                    redisTemplate.opsForList().rightPush(destinationQueue, poppedMessage);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 增加值的访问次数
     *
     * @param valueKey
     */
    public void incrementValueAccessCount(String key, String valueKey, Integer delta) {
        if (StringUtils.isEmpty(key)) key = "valueCounts";
        if (ObjectUtil.isEmpty(delta)) delta = 1;
        hashOperations.increment(key, valueKey, delta);
    }

    /**
     * 获取指定值的访问次数
     *
     * @param valueKey
     * @return
     */
    public long getValueAccessCount(String key, String valueKey) {
        if (StringUtils.isEmpty(key)) key = "valueCounts";
        String count = hashOperations.get(key, valueKey);
        return (count != null) ? Long.parseLong(count) : 0;
    }

    /**
     * 获取所有值的访问次数
     *
     * @return
     */
    public Map<String, String> getAllValueAccessCounts(String key) {
        if (StringUtils.isEmpty(key)) key = "valueCounts";
        return hashOperations.entries(key);
    }

    /**
     * 移除指定值的访问次数
     *
     * @return
     */
    public void removeValue(String key, String valueKey) {
        if (StringUtils.isEmpty(key)) key = "valueCounts";
        hashOperations.delete(key, valueKey);
    }

}
