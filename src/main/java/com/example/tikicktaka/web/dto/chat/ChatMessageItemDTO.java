package com.example.tikicktaka.web.dto.chat;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageItemDTO {
    private Long id;
    private Long senderId;
    private String senderName;     // 닉네임(차단 시 "차단됨")
    private String content;        // 차단 시 null
    private LocalDateTime createdAt;
    private boolean fromBlockedUser;
    private String senderRole;      // "OWNER" or "MEMBER"
}
