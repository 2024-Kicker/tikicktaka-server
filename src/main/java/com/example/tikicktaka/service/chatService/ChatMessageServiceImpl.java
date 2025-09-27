package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.ProfileImgRepository;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.images.ProfileImg;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final StringRedisTemplate redisTemplate;
    private final SocketIOServer socketIOServer;
    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;
    private final MemberRepository memberRepository;
    private final ProfileImgRepository profileImgRepository;

    private static final String REDIS_CHAT_KEY_PREFIX = "companionPostChat:";

    // 메시지 저장
    @Override
    @Transactional
    public ChatMessage sendMessage(String roomId, Long senderId, String message) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("roomId is blank");
        }
        roomId = roomId.trim();

        // 1) Redis 저장
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO(senderId, message);
        ObjectMapper objectMapper = new ObjectMapper();
        final String key = REDIS_CHAT_KEY_PREFIX + roomId;
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessageDTO);
            redisTemplate.opsForList().rightPush(key, jsonMessage);
            redisTemplate.expire(key, 14, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 변환 오류", e);
        }

        // 2) DB 저장
        ChatRoom room = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        ChatMessage entity = ChatMessage.create(room, senderId, message);
        ChatMessage saved = companionPostChatMessageRepository.save(entity);

        // 3) 방의 최근 활동 시각 갱신
        LocalDateTime lastAt = (saved.getTimestamp() != null) ? saved.getTimestamp() : LocalDateTime.now();
        room.setUpdatedAt(lastAt);
        companionPostChatRoomRepository.save(room);

        return saved;
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

//    // 그룹 메시지 전송
//    @Override
//    public void sendGroupMessage(String roomId, Long senderId, String message) {
//
//        // (1) 채팅방 검증
//        ChatRoom room = companionPostChatRoomRepository.findByRoomId(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
//
//        // (2) 저장: 방금 저장된 엔티티 수신
//        ChatMessage saved = saveMessage(roomId, senderId, message);
//
//        // (3) 닉네임/프로필 조회
//        String senderName = memberRepository.findById(saved.getSenderId())
//                .map(Member::getName)
//                .orElse(null);
//
//        String senderProfileUrl = profileImgRepository.findByMember_Id(saved.getSenderId())
//                .map(ProfileImg::getUrl)
//                .orElse(null);
//
//        Long ownerId = resolveOwnerId(room);
//        String senderRole = (ownerId != null && ownerId.equals(saved.getSenderId())) ? "OWNER" : "MEMBER";
//        String createdAt = (saved.getTimestamp() != null)
//                ? saved.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
//                : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//
//        // (4) 최종 스키마 payload 구성
//        Map<String, Object> payload = new LinkedHashMap<>();
//        payload.put("id", saved.getId());
//        payload.put("roomId", roomId);
//        payload.put("senderId", saved.getSenderId());
//        payload.put("senderName", senderName);
//        payload.put("senderRole",senderRole);
//        payload.put("senderProfileUrl", senderProfileUrl);
//        payload.put("message", saved.getMessage());
//        payload.put("createdAt", createdAt);
//
//        socketIOServer.getRoomOperations(roomId).sendEvent("receiveMessage", payload);
//    }
}
