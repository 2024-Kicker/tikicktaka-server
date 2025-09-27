package com.example.tikicktaka.web.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ChatSocketPayload {
    private Long   id;               // DB PK
    private String roomId;
    private Long   senderId;
    private String senderName;       // null 허용
    private String senderProfileUrl; // null 허용
    private String message;
    private String createdAt;        // "2025-09-27T14:12:34Z"
}
