package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CompanionPostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String thumbnailUrl;
    private CompanionPost.PostStatus status;
    private CompanionPost.TravelStatus travelStatus;
    private String chatRoomId;
    private List<String> imageUrls;

    // 필요한 추가 필드가 있다면 추가
    // 예: 작성자 정보, 작성 시간 등

    public CompanionPostResponseDTO(CompanionPost post, List<String> imageUrls) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = (post.getAuthor() != null) ? post.getAuthor().getName() : "Unknown";
        this.status = post.getStatus();
        this.travelStatus = post.getTravelStatus();
        this.thumbnailUrl = post.getThumbnailUrl();
        //this.chatRoomId = post.getChatRoomId();
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>(); // Null 체크
    }
}
