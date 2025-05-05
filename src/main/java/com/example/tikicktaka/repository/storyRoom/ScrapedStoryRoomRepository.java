package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.ScrapedStoryRoomPost;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ScrapedStoryRoomRepository extends JpaRepository<ScrapedStoryRoomPost, Long> {
    Optional<ScrapedStoryRoomPost> findByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    //Optional<ScrapedStoryRoomPost> findByMemberIdAndStoryRoomPost(Long memberId, StoryRoomPost post);
    boolean existsByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    void deleteByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    List<ScrapedStoryRoomPost> findAllByMemberId(Long memberId);
    boolean existsByStoryRoomPostId(Long id);
    //Optional<Object> findByMemberIdAndStoryRoomPostId(Long memberId, Long postId);

}

