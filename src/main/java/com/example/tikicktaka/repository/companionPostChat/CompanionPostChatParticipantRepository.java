package com.example.tikicktaka.repository.companionPostChat;

import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompanionPostChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // 특정 채팅방의 모든 참여자 조회
    @EntityGraph(attributePaths = { "member", "member.profileImg" })
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    // 특정 채팅방에 참가한 사용자 수를 반환
    int countByChatRoom(ChatRoom chatRoom);

    // 특정 사용자가 특정 채팅방에 참여 중인지 확인
    //Optional<ChatParticipant> findByChatRoomAndUserId(ChatRoom chatRoom, Long userId);

    // 특정 사용자를 채팅방에서 제거
    void deleteByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 특정 채팅방과 사용자에 해당하는 참가자 찾기
    ChatParticipant findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    // 채팅방과 사용자의 참여 여부 확인
    boolean existsByChatRoomAndMemberId(ChatRoom chatRoom, Long memberId);

    void deleteByChatRoom(ChatRoom chatRoom);

    // 특정 방의 참가자 수 (roomId 문자열로 바로 세기)
    int countByChatRoom_RoomId(String roomId);

    Optional<ChatParticipant> findFirstByChatRoom_RoomIdAndMember_IdNot(String roomId, Long notMemberId);

    List<ChatParticipant> findByMember_Id(Long memberId, Pageable pageable);
    Optional<ChatParticipant> findByChatRoom_RoomIdAndMember_Id(String roomId, Long memberId);
    List<ChatParticipant> findByChatRoom_CompanionPost_IdAndChatRoom_RoomId(Long postId, String roomId);
    List<ChatParticipant> findByChatRoom_RoomId(String roomId);

}
