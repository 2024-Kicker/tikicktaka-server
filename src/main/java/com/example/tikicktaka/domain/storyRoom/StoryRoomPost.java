package com.example.tikicktaka.domain.storyRoom;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.images.StoryRoomImg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StoryRoomPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Enumerated(EnumType.STRING)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    private LimitTime limitTime;

    // Member와 연관관계 설정S
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    private LocalDateTime createdAt;
    private LocalDateTime enterableUntil;  // 입장 가능 시점 (생성 시점 + 20분)

    @Enumerated(EnumType.STRING)
    private StoryRoomStatus status; // 이야기해요 / 끝난이야기

    private String thumbnailUrl;

    @OneToMany(mappedBy = "storyRoomPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryRoomParticipant> participants = new ArrayList<>();

    // StoryRoomImage와 연관관계 설정
    @OneToMany(mappedBy = "storyRoomPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryRoomImg> images = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addImage(StoryRoomImg image) {
        images.add(image);
        image.setStoryRoomPost(this);
    }

}

