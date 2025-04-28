package com.example.tikicktaka.domain.companionPost;
import com.example.tikicktaka.domain.member.Member;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "companion_post_id")
    private CompanionPost companionPost;

    public ScrapedPost(Member member, CompanionPost post) {
        this.member = member;
        this.companionPost = post;
    }
}

