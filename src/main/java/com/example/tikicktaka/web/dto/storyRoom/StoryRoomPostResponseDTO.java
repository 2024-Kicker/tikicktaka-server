package com.example.tikicktaka.web.dto.storyRoom;

import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.images.StoryRoomImg;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class StoryRoomPostResponseDTO {

    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Topic topic;
    private LimitTime limitTime;
    private StoryRoomStatus status;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime enterableUntil;
    private List<String> imageUrls;
    private int participantCount;
    private boolean isScrapped;
    private String roomId;


    public void setRoomId(String roomId) { this.roomId = roomId; }


    public StoryRoomPostResponseDTO(StoryRoomPost post, List<String> imageUrls, int participantCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = (post.getAuthor() != null) ? post.getAuthor().getName() : "Unknown";
        this.topic = post.getTopic();
        this.limitTime = post.getLimitTime();
        this.status = post.getStatus();
        this.thumbnailUrl = post.getThumbnailUrl();
        this.createdAt = post.getCreatedAt();
        this.enterableUntil = post.getEnterableUntil();

        // 이미지 URL 리스트 설정 (이미지가 없다면 빈 리스트)
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.participantCount= participantCount;

    }

    //로그인한 사용자 게시글 상세 조회
    public StoryRoomPostResponseDTO(StoryRoomPost post, List<String> imageUrls, int participantCount, boolean isScrapped) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = (post.getAuthor() != null) ? post.getAuthor().getName() : "Unknown";
        this.topic = post.getTopic();
        this.limitTime = post.getLimitTime();
        this.status = post.getStatus();
        this.thumbnailUrl = post.getThumbnailUrl();
        this.createdAt = post.getCreatedAt();
        this.enterableUntil = post.getEnterableUntil();

        this.imageUrls = imageUrls;
        this.participantCount = participantCount;
        this.isScrapped = isScrapped;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
