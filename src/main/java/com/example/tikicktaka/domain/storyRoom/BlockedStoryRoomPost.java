package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blocked_story_room_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedStoryRoomPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "story_room_post_id")
    private StoryRoomPost storyRoomPost;

    public BlockedStoryRoomPost(Member member, StoryRoomPost storyRoomPost) {
        this.member = member;
        this.storyRoomPost = storyRoomPost;
    }
}
