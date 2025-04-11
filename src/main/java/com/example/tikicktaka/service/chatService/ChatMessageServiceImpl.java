package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.tikicktaka.domain.chat.ChatRoom;
import com.example.tikicktaka.repository.chat.ChatRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final StringRedisTemplate redisTemplate;
    private final SocketIOServer socketIOServer;
    private final ChatRoomRepository chatRoomRepository;

    // 메시지 저장
    @Override
    public void saveMessage(String roomId, Long senderId, String message) {
        // ChatMessageDTO 객체 생성
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO(senderId, message);

        // JSON 형식으로 변환해서 저장
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessageDTO);

            // Redis에 메시지 저장 (TTL 2주일 설정)
            redisTemplate.opsForList().rightPush("chat:" + roomId, jsonMessage);

            // Redis에 TTL 설정 (2주일 후 자동 삭제)
            redisTemplate.expire("chat:" + roomId, 14, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 변환 오류", e);
        }
    }

    // 메시지 조회
    @Override
    public List<ChatMessageDTO> getMessages(String roomId) {
        List<String> messages = redisTemplate.opsForList().range("chat:" + roomId, 0, -1);

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
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 메시지 저장 (Redis에 저장)
        saveMessage(roomId, senderId, message);

        // 메시지를 해당 채팅방의 모든 참가자에게 전송 (실시간)
        socketIOServer.getRoomOperations(roomId).sendEvent("newMessage", message);
    }
}

