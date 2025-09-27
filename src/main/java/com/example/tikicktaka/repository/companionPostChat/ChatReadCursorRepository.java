package com.example.tikicktaka.repository.companionPostChat;

import com.example.tikicktaka.domain.companionPostChat.ChatReadCursor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadCursorRepository extends JpaRepository<ChatReadCursor, Long> {
    Optional<ChatReadCursor> findByRoomIdAndMember_Id(String roomId, Long memberId);
    boolean existsByRoomIdAndMember_Id(String roomId, Long memberId);
}
