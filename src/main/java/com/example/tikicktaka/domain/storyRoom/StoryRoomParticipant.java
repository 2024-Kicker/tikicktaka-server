package com.example.tikicktaka.domain.storyRoom;

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

    private Long storyRoomId;
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        OWNER, PARTICIPANT
    }

    // getters/setters
}

