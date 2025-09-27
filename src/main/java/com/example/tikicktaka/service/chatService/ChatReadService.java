package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;

public interface ChatReadService {
    ChatMessagePageDTO readCompanionMessages(Long meId, String roomId,
                                             String dir, Long cursor, int size);
    void markRead(String roomId, Long memberId, Long lastReadMessageId);
    int countUnread(String roomId, Long memberId);
}
