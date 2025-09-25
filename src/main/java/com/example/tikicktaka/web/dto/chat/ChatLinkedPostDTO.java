package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

 //내가 참여 중인 동행찾기 채팅방과 연계된 게시글
@Getter
@AllArgsConstructor
public class ChatLinkedPostDTO {
    private final Long postId;
    private final String title;
    private final String content;
    private final String thumbnailUrl;
    private final LocalDateTime createdAt;
}
