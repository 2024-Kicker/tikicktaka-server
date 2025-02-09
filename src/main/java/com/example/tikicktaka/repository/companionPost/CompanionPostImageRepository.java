package com.example.tikicktaka.repository.companionPost;

import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanionPostImageRepository extends JpaRepository<CompanionPostImg, Long> {

    // 특정 게시글에 속한 모든 이미지 조회
    List<CompanionPostImg> findByCompanionPost(CompanionPost companionPost);
}

