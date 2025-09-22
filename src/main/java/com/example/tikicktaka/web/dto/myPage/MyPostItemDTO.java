package com.example.tikicktaka.web.dto.myPage;
// src/main/java/com/example/tikicktaka/web/dto/mypage/MyPostItemDTO.java

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class MyPostItemDTO {
    private Long postId;     // 내부 ID
    private String title;
    private String content;
    private String type;     // "COMPANION" | "STORY"
    private String shareUrl; // 예) https://.../api/companionPost/public/12
    private String thumbnailUrl;
}

