package com.example.tikicktaka.web.dto.storyRoom;

import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoryRoomDetailResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Long creatorId;
    private LocalDateTime createdAt;

    public StoryRoomDetailResponseDTO(StoryRoom room) {
        this.id = room.getId();
        this.title = room.getTitle();
        this.content = room.getContent();
        this.creatorId = room.getCreator().getId();
        this.createdAt = room.getCreatedAt();
    }
}
