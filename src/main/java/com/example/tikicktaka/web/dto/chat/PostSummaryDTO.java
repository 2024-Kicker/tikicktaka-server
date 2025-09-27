package com.example.tikicktaka.web.dto.chat;

import java.time.LocalDateTime;

public record PostSummaryDTO(
        Long postId,
        String title,
        String content,
        LocalDateTime createdAt,
        Boolean authorView,
        String inviteCode // 작성자일 때만 값, 아니면 null
) { }
