package com.example.tikicktaka.service.storyChat;

import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;

public interface StoryChatMessageService {

    // 단건 전송(레디스 push + DB 저장)
    void sendMessage(String roomId, Long senderId, String message);

    // 커서 페이지 조회(prev/next 지원), size 기본 50
    ChatMessagePageDTO readMessages(String roomId, Long meId, Integer size, Long cursor, String direction);
}
