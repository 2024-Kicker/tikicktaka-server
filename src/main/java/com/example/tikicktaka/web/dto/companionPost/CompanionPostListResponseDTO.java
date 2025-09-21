package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CompanionPostListResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private boolean isScraped; // 스크랩 여부
    private LocalDateTime createdAt;           // 작성 날짜
    private boolean isMine;                    // 로그인 사용자가 작성한 글인지
    private CompanionPost.PostStatus status;   // FINDING / FOUND


    public CompanionPostListResponseDTO(CompanionPost post, boolean isScraped, Long memberID) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.thumbnailUrl = post.getThumbnailUrl();  // 대표 이미지 (썸네일) 설정
        this.authorName = post.getAuthor().getName();
        this.isScraped = isScraped;
        this.createdAt = post.getCreatedAt();
        this.status = post.getStatus();
        this.isMine = (memberID != null
                && post.getAuthor() != null
                && post.getAuthor().getId() != null
                && post.getAuthor().getId().equals(memberID));


    }
}
