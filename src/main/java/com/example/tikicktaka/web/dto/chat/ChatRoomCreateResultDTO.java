package com.example.tikicktaka.web.dto.chat;

import com.example.tikicktaka.domain.enums.TargetType;
import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class ChatRoomCreateResultDTO {
    private String roomId;
    private TargetType targetType; // COMPANION_POST, STORY_POST 등 (이미 프로젝트에 있음)
    private Long targetId;         // postId
    private Long ownerId;          // 방장(글 작성자)
    private Long requesterId;      // 방 생성 요청자
}

