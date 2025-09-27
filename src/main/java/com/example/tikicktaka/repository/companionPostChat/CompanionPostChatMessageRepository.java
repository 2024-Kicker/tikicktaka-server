package com.example.tikicktaka.repository.companionPostChat;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompanionPostChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
    void deleteByChatRoom(ChatRoom chatRoom);
    // 최초 진입: 최신부터 N개
    List<ChatMessage> findByChatRoomRoomIdOrderByIdDesc(String roomId, Pageable pageable);

    // 이전 페이지: id < cursor 내림차 N개
    List<ChatMessage> findByChatRoomRoomIdAndIdLessThanOrderByIdDesc(String roomId, Long cursor, Pageable pageable);

    // 다음 페이지: id > cursor 오름차 N개
    List<ChatMessage> findByChatRoomRoomIdAndIdGreaterThanOrderByIdAsc(String roomId, Long cursor, Pageable pageable);

    // 마지막 메시지 1건
    Optional<ChatMessage> findTop1ByChatRoom_RoomIdOrderByIdDesc(String roomId);

    long countByChatRoom_RoomIdAndIdGreaterThan(String roomId, Long id);
    long countByChatRoom_RoomId(String roomId); // lastRead 없을 때 전체를 미읽음으로
    // === 추가: (간이) 안읽은 계산용 메서드들 ===
    Optional<ChatMessage> findTop1ByChatRoom_RoomIdAndSenderIdOrderByIdDesc(String roomId, Long senderId);
    long countByChatRoomRoomIdAndIdGreaterThanAndSenderIdNot(String roomId, Long id, Long notSenderId);



}
