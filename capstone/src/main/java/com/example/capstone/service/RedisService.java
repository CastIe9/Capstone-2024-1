package com.example.capstone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void saveValue(String key, String value, long timeout, TimeUnit timeUnit) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, value, timeout, timeUnit);
    }

    @Transactional
    public void setValue(String key, String value){
        redisTemplate.opsForValue().set(key, value);
    }

    public String getValue(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }

    @Transactional
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
