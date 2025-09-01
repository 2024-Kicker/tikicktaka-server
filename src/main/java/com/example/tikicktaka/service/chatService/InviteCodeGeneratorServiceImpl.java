package com.example.tikicktaka.service.chatService;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;  // TimeUnit 임포트 추가

@Service
public class InviteCodeGeneratorServiceImpl implements InviteCodeGeneratorService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    CompanionPostChatRoomRepository companionPostChatRoomRepository;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 랜덤 코드로 사용할 문자 집합
    private static final int CODE_LENGTH = 8; // 코드 길이


    @Override
    public String generateInviteCode() {
        Random random = new Random();
        StringBuilder inviteCode = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            inviteCode.append(CHARACTERS.charAt(randomIndex));
        }

        //String redisKey = "inviteCode:" + inviteCode.toString();
        //redisTemplate.opsForValue().set(redisKey, inviteCode.toString(), 10, TimeUnit.MINUTES);

        // 3. 초대 코드 반환
        return inviteCode.toString();
    }

    @Override
    public boolean isInviteCodeValid(String inviteCode) {
        //String redisKey = "inviteCode:" + inviteCode;
        //return redisTemplate.hasKey(redisKey);  // Redis에 초대 코드가 존재하는지 확인
        return companionPostChatRoomRepository.findByInviteCode(inviteCode).isPresent();

    }
}