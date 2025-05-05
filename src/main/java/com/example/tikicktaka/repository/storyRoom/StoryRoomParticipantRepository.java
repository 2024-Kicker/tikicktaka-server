package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRoomParticipantRepository extends JpaRepository<StoryRoomParticipant, Long> {
    int countByStoryRoomId(Long id);
}

