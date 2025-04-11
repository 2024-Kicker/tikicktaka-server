package com.example.tikicktaka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 초대 코드 Redis에 저장 (10분 만료)
    public void storeInviteCode(String inviteCode, int expiryTimeInSeconds) {
        stringRedisTemplate.opsForValue().set(inviteCode, "valid", expiryTimeInSeconds, TimeUnit.SECONDS);
    }

    // 초대 코드 유효성 검사 (Redis에서 초대 코드 존재 여부 확인)
    public boolean isInviteCodeValid(String inviteCode) {
        return stringRedisTemplate.hasKey(inviteCode);
    }
}

