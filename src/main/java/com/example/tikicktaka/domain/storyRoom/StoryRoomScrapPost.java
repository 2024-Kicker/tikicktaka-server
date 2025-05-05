package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_room_scrap_ost")
public class StoryRoomScrapPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private StoryRoomPost storyRoomPost;

    private LocalDateTime createdAt;
}
