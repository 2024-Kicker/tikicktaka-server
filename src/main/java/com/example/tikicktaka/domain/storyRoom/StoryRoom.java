package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StoryRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

//    private Long creatorId;
//
//    private Long postId;

    // 연관관계 매핑 (작성자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    // 연관관계 매핑 (게시글)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private StoryRoomPost post;

    @Column(nullable = false, unique = true)
    private String roomId;  // 채팅방 ID

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "storyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryRoomParticipant> participants = new ArrayList<>();


    public StoryRoom(String title, String content, Member creator, StoryRoomPost post, String roomId) {
        this.title = title;
        this.content = content;
        this.creator = creator;
        this.post = post;
        this.roomId = roomId;
        this.createdAt = LocalDateTime.now();
    }

    public StoryRoom(StoryRoomPost post, Member creator) {
        this.post = post;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();  // 이 부분도 필요
    }

    @PrePersist
    public void generateRoomId() {
        if (this.roomId == null) {
            this.roomId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


}
