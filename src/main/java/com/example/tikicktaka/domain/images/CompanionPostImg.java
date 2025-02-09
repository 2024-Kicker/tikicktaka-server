package com.example.tikicktaka.domain.images;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import com.example.tikicktaka.domain.companionPost.CompanionPost;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanionPostImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // 이미지 URL 또는 경로

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companion_post_id", nullable = false)
    private CompanionPost companionPost;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 게시글 설정 메서드 (연관관계 편의 메서드)
    public void setCompanionPost(CompanionPost companionPost) {
        this.companionPost = companionPost;
    }

}
