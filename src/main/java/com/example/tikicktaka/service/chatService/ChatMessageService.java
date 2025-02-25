package com.example.tikicktaka.service.chatService;

import java.util.List;

public interface ChatMessageService {
    void saveMessage(String roomId, String message); // 메시지 저장
    List<String> getMessages(String roomId); // 메시지 조회
}

