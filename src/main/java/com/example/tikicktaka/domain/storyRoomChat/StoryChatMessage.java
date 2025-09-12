package com.example.tikicktaka.domain.storyRoomChat;
import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class StoryChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "story_room_id", nullable = false)
    private StoryRoom storyRoom;

    @Column(nullable = false) private Long senderId;
    @Column(nullable = false, length = 2000) private String message;
    @Column(nullable = false) private LocalDateTime timestamp;

    public static StoryChatMessage create(StoryRoom room, Long senderId, String message) {
        return StoryChatMessage.builder()
                .storyRoom(room).senderId(senderId).message(message)
                .timestamp(LocalDateTime.now()).build();
    }
}

