package com.example.tikicktaka.web.dto.chat;


import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class ChatGroupInviteResultDTO {
    private String roomId;
    private String inviteCode;
    private Long postId;
    private Long ownerId;
    private Integer expiresInSeconds; // ex) 600 (옵션)
    private LocalDateTime createdAt;  // (옵션)
}
