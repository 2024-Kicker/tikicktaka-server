package com.example.tikicktaka.service.storyRoom;

import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryRoomStatusScheduler {

    private final StoryRoomPostRepository storyRoomPostRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000) // 5분마다 실행
    public void updateExpiredStoryRooms() {
        LocalDateTime now = LocalDateTime.now();

        List<StoryRoomPost> inProgressPosts = storyRoomPostRepository.findAllByStatus(StoryRoomStatus.IN_PROGRESS);

        for (StoryRoomPost post : inProgressPosts) {
            LocalDateTime deadline = post.getCreatedAt().plusMinutes(post.getLimitTime().getMinutes());
            if (now.isAfter(deadline)) {
                post.setStatus(StoryRoomStatus.ENDED);
                log.info("이야기방 종료됨: postId={}, title={}", post.getId(), post.getTitle());
            }
        }

        storyRoomPostRepository.saveAll(inProgressPosts);
    }
}

