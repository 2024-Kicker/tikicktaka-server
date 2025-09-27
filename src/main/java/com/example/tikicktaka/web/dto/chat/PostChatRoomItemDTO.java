package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PostChatRoomItemDTO {
    private final String roomId;
    private final boolean group; // 단체면 true, 1:1이면 false
    private final Long postId;
    private final Integer participants; // 참가자 수
    private final List<String> participantProfileImages; // (요청자 포함) 모든 참가자 프로필 이미지 URL
    private final String lastMessage;
    private final LocalDateTime lastAt;
    private final Integer unreadCount; // (임시) 내 마지막 발신 이후 상대 메시지 수
}
