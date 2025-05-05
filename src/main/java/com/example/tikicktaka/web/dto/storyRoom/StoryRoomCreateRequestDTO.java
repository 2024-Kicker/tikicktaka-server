package com.example.tikicktaka.web.dto.storyRoom;

import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.Topic;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoryRoomCreateRequestDTO {
    private String title;
    private String content;
    private Topic topic;
    private LimitTime limitTime;
}


