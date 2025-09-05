package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomSummaryDTO {
    private final String roomId;
    private final String type;            // "DIRECT" | "GROUP" 추후에 STORYROOM 확장?
    private final boolean group;          // 단체방 여부
    private final Long postId;

    // 게시글 헤더
    private final String postTitle;
    private final LocalDateTime postCreatedAt;
    private final String postContent;     // 요약/일부/전체 중 편한 값
    private final String postShareLink;

    // 권한/정책
    private final boolean authorView;     // 작성자 뷰
    private final String inviteCode;      // 작성자면 단체방 코드 노출(없으면 null)

    // 상태/요약
    private final Integer participants;
    private final String lastMessage;
    private final LocalDateTime lastAt;
}

