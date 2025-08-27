// src/main/java/com/example/tikicktaka/domain/mapping/scrap/CompanionPostScrap.java
package com.example.tikicktaka.domain.mapping.scrap;

import com.example.tikicktaka.domain.common.BaseDateTimeEntity;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "companion_post_scrap",
        uniqueConstraints = @UniqueConstraint(name = "uk_companion_scrap_member_post",
                columnNames = {"member_id", "companion_post_id"}),
        indexes = {
                @Index(name = "idx_companion_scrap_member", columnList = "member_id"),
                @Index(name = "idx_companion_scrap_post", columnList = "companion_post_id")
        })
public class CompanionPostScrap extends BaseDateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "companion_post_scrap_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "companion_post_id", nullable = false)
    private CompanionPost companionPost;
}
