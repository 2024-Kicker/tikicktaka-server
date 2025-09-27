package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostChatRoomListResponseDTO {
    private final List<PostChatRoomItemDTO> rooms;
}
