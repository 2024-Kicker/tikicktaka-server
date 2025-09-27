package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatRoomSummaryDTO;
import com.example.tikicktaka.web.dto.chat.PostSummaryDTO;

public interface ChatRoomSummaryService {
    ChatRoomSummaryDTO getRoomSummary(Long meId, String roomId);
    PostSummaryDTO getPostHeader(Long meId, Long postId);

}

