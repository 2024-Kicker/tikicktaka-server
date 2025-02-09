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
    private String thumbnailUrl;
    private CompanionPost.PostStatus status;

    // 필요한 추가 필드가 있다면 추가
    // 예: 작성자 정보, 작성 시간 등

    public CompanionPostResponseDTO(CompanionPost post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.thumbnailUrl = post.getThumbnailUrl();
//        this.imageUrls = (post.getImages() != null)
//                ? post.getImages().stream()
//                .map(CompanionPostImg::getImageUrl)
//                .collect(Collectors.toList())
//                : new ArrayList<>();
        this.status = post.getStatus();
    }
}
