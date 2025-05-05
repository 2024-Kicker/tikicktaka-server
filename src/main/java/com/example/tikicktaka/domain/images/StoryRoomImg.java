package com.example.tikicktaka.domain.images;

import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoryRoomImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_room_post_id", nullable = false)
    private StoryRoomPost storyRoomPost;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void setStoryRoomPost(StoryRoomPost post) {
        this.storyRoomPost = post;
    }
}
