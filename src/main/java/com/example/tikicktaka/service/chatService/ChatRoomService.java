package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatRoomDTO;
import java.util.Optional;

public interface ChatRoomService {

    // 채팅방 ID로 채팅방 조회
    Optional<ChatRoomDTO> getChatRoomById(String roomId);
    // 1:1 채팅방 생성
    String createOneOnOneChatRoom(Long userId, Long targetUserId,Long postId );


    // 채팅방 삭제
    void deleteChatRoom(String roomId);

    String joinRoomByInviteCode(String inviteCode, Long userId);

    ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO);

    // 게시글에 해당하는 채팅방 존재 여부 확인
    boolean existsByCompanionPost_Id(Long postId);

    void deleteRoomsByPostId(Long postId);
}

