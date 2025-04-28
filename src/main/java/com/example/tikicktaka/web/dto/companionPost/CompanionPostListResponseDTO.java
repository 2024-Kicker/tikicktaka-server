package com.example.tikicktaka.web.dto.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import lombok.Getter;

@Getter
public class CompanionPostListResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private boolean isScraped; // 스크랩 여부


    public CompanionPostListResponseDTO(CompanionPost post, boolean isScraped) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.thumbnailUrl = post.getThumbnailUrl();  // 대표 이미지 (썸네일) 설정
        this.authorName = post.getAuthor().getName();
        this.isScraped = isScraped;

    }
}
