package com.example.tikicktaka.web.dto.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import lombok.*;

@Getter
public class StoryRoomListResponseDTO {
    private Long id;
    private String title;

    public StoryRoomListResponseDTO(StoryRoom room) {
        this.id = room.getId();
        this.title = room.getTitle();
    }
}
