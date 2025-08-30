package com.example.tikicktaka.repository.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long> {
    // 특정 회원이 작성한 게시글 조회
    List<CompanionPost> findByAuthor (Member member);

    // 특정 상태(Finding, Found)의 게시글 조회
    List<CompanionPost> findByStatus(CompanionPost.PostStatus status);

    // 제목에 특정 단어가 포함된 게시글 조회
    @Query("SELECT p FROM CompanionPost p WHERE p.title LIKE %:keyword%")
    List<CompanionPost> searchByTitle(@Param("keyword") String keyword);

    //게시글 목록을 페이징해서 가져오기 (최신순)
    //Page<CompanionPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM CompanionPost p WHERE p.id NOT IN :blockedPostIds ORDER BY p.createdAt DESC")
    Page<CompanionPost> findAllByIdNotInOrderByCreatedAtDesc(@Param("blockedPostIds") List<Long> blockedPostIds, Pageable pageable);

    @Query("SELECT p FROM CompanionPost p ORDER BY p.createdAt DESC")
    Page<CompanionPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM CompanionPost p WHERE p.id IN :postIds")
    List<CompanionPost> findAllByIdIn(@Param("postIds") List<Long> postIds);

    // ✅ 파생 쿼리만으로 필터 조합
    Page<CompanionPost> findByStatusIn(List<CompanionPost.PostStatus> statuses, Pageable pageable);
    Page<CompanionPost> findByStatusInAndIdNotIn(List<CompanionPost.PostStatus> statuses, List<Long> excludedIds, Pageable pageable);

    Page<CompanionPost> findByIdInAndStatusIn(List<Long> ids, List<CompanionPost.PostStatus> statuses, Pageable pageable);
    Page<CompanionPost> findByIdInAndStatusInAndIdNotIn(List<Long> ids, List<CompanionPost.PostStatus> statuses, List<Long> excludedIds, Pageable pageable);

}
