package com.example.capstone;

import com.example.capstone.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisServiceTests {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void testSetAndGetValues() {
        String key = "testKey";
        String value = "testValue";
        redisService.setValues(key, value);
        String result = redisService.getValues(key);
        assertThat(result).isEqualTo(value);
    }

    @Test
    void testSetValuesWithTimeout() throws InterruptedException {
        String key = "testKeyTimeout";
        String value = "testValueTimeout";
        long timeout = 1000; // 1 second
        redisService.setValuesWithTimeout(key, value, timeout);
        Thread.sleep(1100); // wait for the key to expire
        String result = redisService.getValues(key);
        assertThat(result).isNull();
    }

    @Test
    void testDeleteValues() {
        String key = "testKeyDelete";
        String value = "testValueDelete";
        redisService.setValues(key, value);
        redisService.deleteValues(key);
        String result = redisService.getValues(key);
        assertThat(result).isNull();
    }
}
