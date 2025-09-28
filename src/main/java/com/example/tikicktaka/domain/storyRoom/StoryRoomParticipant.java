
package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StoryRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //private Long storyRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_room_id", nullable = false)
    private StoryRoom storyRoom;

    // StoryRoomPost와의 관계 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_room_post_id", nullable = false)  // StoryRoomPost의 ID를 저장
    private StoryRoomPost storyRoomPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        OWNER, PARTICIPANT
    }

    // getters/setters
}

