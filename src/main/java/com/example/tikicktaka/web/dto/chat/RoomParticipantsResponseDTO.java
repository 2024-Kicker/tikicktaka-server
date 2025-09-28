package com.example.tikicktaka.web.dto.chat;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class RoomParticipantsResponseDTO {
    private String type;     // "COMPANION" | "STORY"
    private String roomId;
    private List<ChatParticipantDTO> participants;
}