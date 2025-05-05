package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.images.StoryRoomImg;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRoomImageRepository extends JpaRepository<StoryRoomImg, Long> {

    // 특정 이야기방 게시글에 속한 모든 이미지 조회
    List<StoryRoomImg> findByStoryRoomPost(StoryRoomPost storyRoomPost);
}
