package com.example.tikicktaka.service.storyChat;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoomChat.StoryChatMessage;
import com.example.tikicktaka.repository.storyRoomChat.StoryRoomChatMessageRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessageItemDTO;
import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryChatMessageServiceImpl implements StoryChatMessageService {

    private final StoryRoomRepository storyRoomRepository;
    private final StoryRoomChatMessageRepository messageRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String redisKey(String roomId) {
        return "storychat:room:" + roomId + ":messages";
    }

//    @Override
//    @Transactional
//    public void sendMessage(String roomId, Long senderId, String message) {
//        // 방 확인
//        StoryRoom room = storyRoomRepository.findByRoomId(roomId)
//                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));
//
//        // DB 저장
//        StoryChatMessage saved = messageRepository.save(StoryChatMessage.create(room, senderId, message));
//
//        // Redis에도 push (최신이 오른쪽)
//        Map<String, Object> payload = new LinkedHashMap<>();
//        payload.put("id", saved.getId());
//        payload.put("senderId", saved.getSenderId());
//        payload.put("message", saved.getMessage());
//        payload.put("timestamp", saved.getTimestamp().toString());
//
//        try {
//            stringRedisTemplate.opsForList().rightPush(redisKey(roomId), objectMapper.writeValueAsString(payload));
//            // 필요 시 trim으로 길이 제한 가능: e.g. 최신 500개만 유지
//            // stringRedisTemplate.opsForList().trim(redisKey(roomId), -500, -1);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("메시지 직렬화 오류", e);
//        }
//    }

    @Override
    @Transactional
    public void sendMessage(String roomId, Long senderId, String message) {
        StoryRoom room = storyRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다. roomId=" + roomId));

        StoryChatMessage saved = messageRepository.save(StoryChatMessage.create(room, senderId, message));

        // (선택) Redis 히스토리 캐시
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", saved.getId());
        payload.put("senderId", saved.getSenderId());
        payload.put("message", saved.getMessage());
        payload.put("timestamp", saved.getTimestamp().toString());

        try {
            stringRedisTemplate.opsForList().rightPush(redisKey(roomId), objectMapper.writeValueAsString(payload));
            // stringRedisTemplate.opsForList().trim(redisKey(roomId), -500, -1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 직렬화 오류", e);
        }
    }



    @Override
    @Transactional(readOnly = true)
    public ChatMessagePageDTO readMessages(String roomId, Long meId, Integer size, Long cursor, String direction) {
        int pageSize = (size == null || size <= 0) ? 50 : size;

        // 기본 전략:
        // 1) 커서 없으면: 최신부터 N개 (DESC)
        // 2) direction=prev & cursor<id: 이전 페이지 (DESC)
        // 3) direction=next & cursor>id: 다음 페이지 (ASC) 후 최종 응답은 시간순으로 오름차 정렬 or 그대로 반환 로직 결정
        List<StoryChatMessage> rows;

        if (cursor == null) {
            rows = messageRepository.findByStoryRoom_RoomIdOrderByIdDesc(roomId, PageRequest.of(0, pageSize));
        } else if ("next".equalsIgnoreCase(direction)) {
            rows = messageRepository.findByStoryRoom_RoomIdAndIdGreaterThanOrderByIdAsc(roomId, cursor, PageRequest.of(0, pageSize));
        } else {
            // default: prev
            rows = messageRepository.findByStoryRoom_RoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, PageRequest.of(0, pageSize));
        }

        List<ChatMessageItemDTO> items = rows.stream()
                .map(m -> ChatMessageItemDTO.builder()
                        .id(m.getId())
                        .senderId(m.getSenderId())
                        .senderName(null)
                        .content(m.getMessage())
                        .createdAt(m.getTimestamp())
                        .fromBlockedUser(false)
                        .senderRole(null)
                        .build())
                .collect(Collectors.toList());

        // prev/next 커서 계산
        Long prevCursor = items.isEmpty() ? null : items.get(items.size() - 1).getId(); // DESC로 나왔을 때 끝이 가장 과거
        Long nextCursor;

        if (cursor == null) {
            // 최신 페이지에서 nextCursor는 가장 앞으로(가장 최신) 이동할 커서가 필요 없음 -> 최신 1건 id를 반환해도 되고 null로 둬도 됨
            nextCursor = items.isEmpty() ? null :
                    messageRepository.findTop1ByStoryRoom_RoomIdOrderByIdDesc(roomId).map(StoryChatMessage::getId).orElse(null);
        } else if ("next".equalsIgnoreCase(direction)) {
            // ASC로 가져왔으니 가장 끝이 최신
            nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getId();
            // prevCursor는 가장 앞(가장 과거)
            prevCursor = items.isEmpty() ? null : items.get(0).getId();
        } else {
            // prev 조회(DESC)였으니 nextCursor는 가장 앞(가장 최신)
            nextCursor = items.isEmpty() ? null : items.get(0).getId();
        }

        return ChatMessagePageDTO.builder()
                .authorView(false) // 필요 시 방장 여부 판단 로직 연결
                .prevCursor(prevCursor)
                .nextCursor(nextCursor)
                .messages(items)
                .build();
    }
}
