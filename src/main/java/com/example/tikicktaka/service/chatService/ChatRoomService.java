package com.example.tikicktaka.service.chatService;


import com.example.tikicktaka.domain.chat.ChatRoom;

import java.util.Optional;

public interface ChatRoomService {
    ChatRoom createOneToOneRoom(Long user1, Long user2); // 1:1 채팅방 생성
    ChatRoom createGroupRoom(Long ownerId); // 단체 채팅방 생성
    Optional<ChatRoom> getChatRoom(String roomId); // 특정 채팅방 조회
}


