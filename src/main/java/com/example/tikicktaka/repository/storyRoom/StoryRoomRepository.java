package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryRoomRepository extends JpaRepository<StoryRoom, Long> {
    void deleteByPost(StoryRoomPost post);

    Optional<Object> findByPost(StoryRoomPost post);
}

