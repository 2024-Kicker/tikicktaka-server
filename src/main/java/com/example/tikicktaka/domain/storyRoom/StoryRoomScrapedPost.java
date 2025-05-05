package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryRoomScrapedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "story_room_post_id")
    private StoryRoomPost storyRoomPost;

    private LocalDateTime createdAt;

    public StoryRoomScrapedPost(Member member, StoryRoomPost post) {
        this.member = member;
        this.storyRoomPost = post;
    }
}

