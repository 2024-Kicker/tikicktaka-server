package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;

public interface ChatGateway {
    void send(String roomId, Long senderId, String message);
    ChatMessagePageDTO read(String roomId, Long meId, Integer size, Long cursor, String direction);
}
