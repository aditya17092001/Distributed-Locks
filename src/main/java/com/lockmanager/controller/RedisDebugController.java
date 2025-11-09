package com.lockmanager.controller;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class RedisDebugController {
	private final StringRedisTemplate redisTemplate;

    public RedisDebugController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ping-redis")
    public Map<String, String> pingRedis() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return Map.of("status", "ok", "pong", pong == null ? "null" : pong);
        } catch (Exception e) {
            return Map.of("status", "error", "exception", e.getClass().getSimpleName(), "message", e.getMessage());
        }
    }
    
    @GetMapping("/test-socket")
    public Map<String,String> testSocket() {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("redis-11341.c301.ap-south-1-1.ec2.redns.redis-cloud.com", 11341), 3000);
            socket.close();
            return Map.of("status","ok","message","tcp connect succeeded");
        } catch (Exception e) {
            return Map.of("status","error","message", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
