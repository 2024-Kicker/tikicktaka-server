package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatRoomSummaryDTO;

public interface ChatRoomSummaryService {
    ChatRoomSummaryDTO getSummary(Long meId, String roomId);
}

