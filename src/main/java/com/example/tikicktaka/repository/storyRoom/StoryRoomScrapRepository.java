package com.example.tikicktaka.repository.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoomScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface StoryRoomScrapRepository extends JpaRepository<StoryRoomScrapedPost, Long> {
    Optional<StoryRoomScrapedPost> findByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    boolean existsByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    void deleteByMemberIdAndStoryRoomPostId(Long memberId, Long postId);
    List<StoryRoomScrapedPost> findAllByMemberId(Long memberId);

    boolean existsByStoryRoomPostId(Long id);
}

