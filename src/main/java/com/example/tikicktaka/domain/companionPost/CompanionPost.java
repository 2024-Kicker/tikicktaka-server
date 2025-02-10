package com.example.tikicktaka.domain.companionPost;

import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    // 이미지 리스트 (1:N 관계)
    @OneToMany(mappedBy = "companionPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)  // ✅ 즉시 로딩
    private List<CompanionPostImg> images = new ArrayList<>();

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Enumerated(EnumType.STRING)
    private TravelStatus travelStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Member와 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    public void setThumbnailUrl(String imageUrl) {
        this.thumbnailUrl = imageUrl;
    }

    public enum PostStatus {
        FINDING, // 동행 구하는 중
        FOUND    // 동행 구했음
    }

    public enum TravelStatus {
        Baseball, // 동행 구하는 중
        Travel    // 동행 구했음
    }

    public List<CompanionPostImg> getImages() {
        return images != null ? images : new ArrayList<>();
    }

    public void addImage(CompanionPostImg image) {
        images.add(image);
        image.setCompanionPost(this);

        // 첫 번째 이미지를 썸네일로 저장
        if (thumbnailUrl == null) {
            this.thumbnailUrl = image.getImageUrl();
        }
    }
}
