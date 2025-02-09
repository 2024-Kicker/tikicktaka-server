package com.example.tikicktaka.repository.companionPost;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanionPostRepository extends JpaRepository<CompanionPost, Long> {
    // 특정 회원이 작성한 게시글 조회
    List<CompanionPost> findByAuthor (Member member);

    // 특정 상태(Finding, Found)의 게시글 조회
    List<CompanionPost> findByStatus(CompanionPost.PostStatus status);

    // 제목에 특정 단어가 포함된 게시글 조회
    @Query("SELECT p FROM CompanionPost p WHERE p.title LIKE %:keyword%")
    List<CompanionPost> searchByTitle(@Param("keyword") String keyword);

    //게시글 목록을 페이징해서 가져오기 (최신순)
    Page<CompanionPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
