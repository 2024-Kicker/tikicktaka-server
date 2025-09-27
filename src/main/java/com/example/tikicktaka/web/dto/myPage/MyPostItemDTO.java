package com.example.tikicktaka.web.dto.myPage;
// src/main/java/com/example/tikicktaka/web/dto/mypage/MyPostItemDTO.java

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MyPostItemDTO {
    private Long postId;
    private String title;
    private String content;
    private String type;     // "COMPANION" | "STORY"
    private String shareUrl;
    private String thumbnailUrl;
    private CompanionPost.PostStatus status;   // FINDING / FOUND
    private LocalDateTime createdAt;

}

