package com.example.tikicktaka.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomListResponseDTO {
    private final Long nextCursor;        // 다음 페이지 커서(없으면 null)
    private final List<ChatRoomListItemDTO> rooms;
}
