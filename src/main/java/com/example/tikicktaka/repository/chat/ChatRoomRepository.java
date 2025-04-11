package com.example.tikicktaka.repository.chat;

import com.example.tikicktaka.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);  // roomId로 채팅방 찾기

    Optional<ChatRoom> findByInviteCode(String inviteCode);

    public Optional<ChatRoom> findByCompanionPost_IdAndParticipant_Id(Long postId, Long participantId);

    boolean existsByCompanionPost_Id(Long postId);

    Optional<ChatRoom> findByCompanionPostIdAndOwnerIdAndParticipantId(Long postId, Long ownerId, Long participantId);

    List<ChatRoom> findAllByCompanionPost_Id(Long postId);


}
