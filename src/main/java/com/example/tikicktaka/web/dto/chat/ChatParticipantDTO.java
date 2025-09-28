package com.example.tikicktaka.web.dto.chat;

import lombok.*;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class ChatParticipantDTO {
    private Long memberId;
    private String name;
    private String profileUrl;
}

