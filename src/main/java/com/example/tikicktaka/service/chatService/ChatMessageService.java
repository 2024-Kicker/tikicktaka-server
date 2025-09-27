package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.web.dto.chat.ChatMessageDTO;

import java.util.List;

public interface ChatMessageService {
    ChatMessage sendMessage(String roomId, Long senderId, String message); // 메시지 저장
    List<ChatMessageDTO> getMessages(String roomId); // 메시지 조회

//    void sendGroupMessage(String roomId, Long senderId, String message);
}

