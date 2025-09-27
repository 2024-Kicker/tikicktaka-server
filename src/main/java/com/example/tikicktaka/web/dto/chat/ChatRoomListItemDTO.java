package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomListItemDTO {
    private final String roomId;          // 채팅방 식별자(UUID 등)
    private final String domainType;      // "COMPANION" (추후 "STORY" 확장)
    private final boolean group;          // 단체방 여부
    private final Long postId;            // 동행글 id
    private final String roomTitle;       // 단체: 고정 타이틀, 1:1: 상대 닉네임
    private final Integer participants;   // 참가자 수
    private final String lastMessage;     // 마지막 메시지
    private final LocalDateTime lastAt;   // 마지막 메시지 시각
    private final Long lastMessageId;     // 커서용
    //private final int unreadCount;        // 읽지않은 메시지 수
}
