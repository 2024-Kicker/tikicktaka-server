package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryRoomParticipantRepository extends JpaRepository<StoryRoomParticipant, Long> {
    int countByStoryRoomId(Long id);
    void deleteByStoryRoomIdAndMemberId(Long storyRoomId, Long memberId);
    boolean existsByStoryRoomIdAndMemberId(Long id, Long memberId);
    Optional<StoryRoomParticipant> findByStoryRoom_IdAndMember_Id(Long storyRoomId, Long memberId);
    void deleteByStoryRoom_Post_Id(Long postId);

    // 참가자 목록 (me 제외는 서비스에서 필터)
    @EntityGraph(attributePaths = { "member", "member.profileImg" })
    List<StoryRoomParticipant> findByStoryRoom(StoryRoom storyRoom);
}

