package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryRoomParticipantRepository extends JpaRepository<StoryRoomParticipant, Long> {
    int countByStoryRoomId(Long id);
    void deleteByStoryRoomIdAndMemberId(Long storyRoomId, Long memberId);
    boolean existsByStoryRoomIdAndMemberId(Long id, Long memberId);
    Optional<StoryRoomParticipant> findByStoryRoom_IdAndMember_Id(Long storyRoomId, Long memberId);

}

