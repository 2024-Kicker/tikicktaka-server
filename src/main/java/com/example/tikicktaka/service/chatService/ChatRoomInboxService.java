package com.example.tikicktaka.service.chatService;


import com.example.tikicktaka.web.dto.chat.ChatRoomListResponseDTO;

public interface ChatRoomInboxService {
    ChatRoomListResponseDTO getMyRooms(Long meId, Long cursor, int size);
}
