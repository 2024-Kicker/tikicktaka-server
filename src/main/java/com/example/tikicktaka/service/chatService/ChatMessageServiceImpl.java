package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final StringRedisTemplate redisTemplate;
    private final SocketIOServer socketIOServer;
    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;

    private static final String REDIS_CHAT_KEY_PREFIX = "companionPostChat:";

    // 메시지 저장
    @Override
    @Transactional
    public void saveMessage(String roomId, Long senderId, String message) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is blank");
        }
        roomId = roomId.trim(); // 공백 가드

        // ChatMessageDTO 객체 생성
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO(senderId, message);

        // JSON 형식으로 변환해서 Redis에 저장
        ObjectMapper objectMapper = new ObjectMapper();
        final String key = REDIS_CHAT_KEY_PREFIX + roomId;

        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessageDTO);

            // Redis에 메시지 저장 (TTL 2주일 설정)
            redisTemplate.opsForList().rightPush(key, jsonMessage);

            // Redis에 TTL 설정 (2주일 후 자동 삭제)
            redisTemplate.expire(key, 14, TimeUnit.DAYS);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 변환 오류", e);
        }

        // DB에도 저장
        ChatRoom room = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        ChatMessage entity = ChatMessage.create(room, senderId, message);
        companionPostChatMessageRepository.save(entity);

        // 방의 최근 활동 시각 갱신 (인박스 정렬 기준)
        LocalDateTime lastAt = (entity.getTimestamp() != null) ? entity.getTimestamp() : LocalDateTime.now();
        room.setUpdatedAt(lastAt); // ChatRoom 엔티티에 setUpdatedAt 메서드가 필요
        companionPostChatRoomRepository.save(room);
    }

    // 메시지 조회
    @Override
    public List<ChatMessageDTO> getMessages(String roomId) {
        List<String> messages = redisTemplate.opsForList().range(REDIS_CHAT_KEY_PREFIX + roomId, 0, -1);

        return messages.stream().map(msg -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(msg, ChatMessageDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("메시지 변환 오류", e);
            }
        }).collect(Collectors.toList());
    }

    // 그룹 메시지 전송
    @Override
    public void sendGroupMessage(String roomId, Long senderId, String message) {
        // 채팅방 존재 여부 확인
        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 메시지 저장 (Redis + DB)
        saveMessage(roomId, senderId, message);

        // 메시지를 해당 채팅방의 모든 참가자에게 전송 (실시간)
        socketIOServer.getRoomOperations(roomId).sendEvent("newMessage", message);
    }
}
