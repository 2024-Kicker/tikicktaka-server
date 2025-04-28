package com.example.tikicktaka.repository.companionPost;

import com.example.tikicktaka.domain.companionPost.ScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {
    boolean existsByMemberIdAndCompanionPostId(Long memberId, Long companionPostId);

    void deleteByMemberIdAndCompanionPostId(Long memberId, Long companionPostId);
}
