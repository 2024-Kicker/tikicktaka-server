package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;
import com.example.tikicktaka.service.storyChat.StoryChatMessageService;
import com.example.tikicktaka.service.chatService.ChatReadService; // 동행 쪽 메시지 읽기/보내기 서비스 명에 맞춰 수정
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatGatewayImpl implements ChatGateway {

    private final RoomResolver roomResolver;
    private final StoryChatMessageService storyChatMessageService;
    private final ChatReadService chatReadService;
    private final ChatMessageService chatMessageService;

    @Override
    public void send(String roomId, Long senderId, String message) {
        var r = roomResolver.resolve(roomId).orElseThrow(() -> new IllegalArgumentException("잘못된 roomId"));
        switch (r.type()) {
            case STORY -> storyChatMessageService.sendMessage(roomId, senderId, message);
            case COMPANION -> chatMessageService.sendMessage(roomId, senderId, message);

        }
    }

    @Override
    public ChatMessagePageDTO read(String roomId, Long meId, Integer size, Long cursor, String direction) {
        var r = roomResolver.resolve(roomId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 roomId"));

        int pageSize = (size == null || size <= 0) ? 50 : size;
        String dir = (direction == null || direction.isBlank()) ? "prev" : direction;

        return switch (r.type()) {
            case STORY -> storyChatMessageService.readMessages(roomId, meId, pageSize, cursor, dir);
            case COMPANION -> chatReadService.readCompanionMessages(meId, roomId, dir, cursor, pageSize);
        };
    }
}
