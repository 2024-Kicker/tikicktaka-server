package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.service.chatService.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveMessage(String roomId, String message) {
        redisTemplate.opsForList().rightPush("chat:" + roomId, message);
    }

    @Override
    public List<String> getMessages(String roomId) {
        return redisTemplate.opsForList().range("chat:" + roomId, 0, -1);
    }
}

