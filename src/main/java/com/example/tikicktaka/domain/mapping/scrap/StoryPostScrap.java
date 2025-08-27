// src/main/java/com/example/tikicktaka/domain/mapping/scrap/StoryPostScrap.java
package com.example.tikicktaka.domain.mapping.scrap;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoomPost;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "story_post_scrap",
        uniqueConstraints = @UniqueConstraint(name = "uk_story_scrap_member_post",
                columnNames = {"member_id", "story_post_id"}),
        indexes = {
                @Index(name = "idx_story_scrap_member", columnList = "member_id"),
                @Index(name = "idx_story_scrap_post", columnList = "story_post_id")
        })
public class StoryPostScrap extends BaseDateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_post_scrap_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "story_post_id", nullable = false)
    private StoryRoomPost storyRoomPost;
}
