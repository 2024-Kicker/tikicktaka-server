package com.example.tikicktaka.web.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class JoinRoomRequest {
    @JsonProperty("roomId")
    private String roomId;
    @JsonProperty("userId")
    private Long userId;
}

