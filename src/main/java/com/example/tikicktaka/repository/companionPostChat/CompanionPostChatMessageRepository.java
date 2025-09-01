package com.example.tikicktaka.repository.companionPostChat;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanionPostChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
    void deleteByChatRoom(ChatRoom chatRoom);
    // 최초 진입: 최신부터 N개
    List<ChatMessage> findByChatRoomRoomIdOrderByIdDesc(String roomId, Pageable pageable);

    // 이전 페이지: id < cursor 내림차 N개
    List<ChatMessage> findByChatRoomRoomIdAndIdLessThanOrderByIdDesc(String roomId, Long cursor, Pageable pageable);

    // 다음 페이지: id > cursor 오름차 N개
    List<ChatMessage> findByChatRoomRoomIdAndIdGreaterThanOrderByIdAsc(String roomId, Long cursor, Pageable pageable);

}
