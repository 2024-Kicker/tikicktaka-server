package com.example.tikicktaka.repository.companionPost;

import com.example.tikicktaka.domain.companionPost.ScrapedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapedPostRepository extends JpaRepository<ScrapedPost, Long> {
    boolean existsByMemberIdAndCompanionPostId(Long memberId, Long companionPostId);

    void deleteByMemberIdAndCompanionPostId(Long memberId, Long companionPostId);

    Optional<Object> findByMemberIdAndCompanionPostId(Long memberId, Long postId);
}
