package com.example.tikicktaka.web.dto.myPage;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScrapCompanionPostDTO {
    private Long id;                 // post id
    private String title;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private String status;           // 필요 시 enum name
    private LocalDateTime createdAt; // post 생성일
    private boolean scrapped;        // 항상 true
}

