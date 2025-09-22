package com.example.tikicktaka.web.dto.myPage;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScrapStoryRoomPostDTO {
    private Long id;
    private String title;
    private String content;
    private String topic;            // 필요 시 문자열/enum name
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private boolean scrapped;        // true
}
