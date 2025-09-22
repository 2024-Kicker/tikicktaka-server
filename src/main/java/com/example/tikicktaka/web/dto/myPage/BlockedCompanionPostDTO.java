package com.example.tikicktaka.web.dto.myPage;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedCompanionPostDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private String status;           // 필요 없으면 null
    private LocalDateTime createdAt;
    private boolean blocked;         // 항상 true
}
