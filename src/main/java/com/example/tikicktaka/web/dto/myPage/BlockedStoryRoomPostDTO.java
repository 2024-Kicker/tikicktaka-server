package com.example.tikicktaka.web.dto.myPage;
import java.time.LocalDateTime;

public record BlockedStoryRoomPostDTO(
        Long id,
        String title,
        String authorName,
        String status,           // 필요 없으면 제거해도 됨
        LocalDateTime createdAt,
        boolean blocked
) {}