package com.example.tikicktaka.repository.companionPostChat;

import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanionPostChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);  // roomId로 채팅방 찾기

    Optional<ChatRoom> findByInviteCode(String inviteCode);

    public Optional<ChatRoom> findByCompanionPost_IdAndParticipant_Id(Long postId, Long participantId);

    boolean existsByCompanionPost_Id(Long postId);

    Optional<ChatRoom> findByCompanionPostIdAndOwnerIdAndParticipantId(Long postId, Long ownerId, Long participantId);

    List<ChatRoom> findAllByCompanionPost_Id(Long postId);

    boolean existsByCompanionPost_IdAndIsGroupTrue(Long postId);

    // 해당 게시글의 단체방 (1개만 존재 가정)
    Optional<ChatRoom> findFirstByCompanionPost_IdAndIsGroupTrue(Long companionPostId);
}
