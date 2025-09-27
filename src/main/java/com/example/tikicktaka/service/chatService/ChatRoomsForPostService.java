package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.PostChatRoomListResponseDTO;

public interface ChatRoomsForPostService {
    PostChatRoomListResponseDTO myRoomsForPost(Long meId, Long postId);
}
