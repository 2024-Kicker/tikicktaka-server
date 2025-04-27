
package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CompanionPostService {


    @Transactional
    CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.TravelStatus travelStatus);

    @Transactional
    CompanionPost deletePost (Long postId, Long memberID); //게시글 삭제 기능

//    Page<CompanionPostListResponseDTO> getPostList(Pageable pageable); //게시글 목록 조회
//
CompanionPostResponseDTO getPostDetail(Long postId); //게시글 상세 조회

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    CompanionPostResponseDTO getPostDetail(Long postId, Long memberId);

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    Page<CompanionPostListResponseDTO> getPostList(Long memberId, Pageable pageable);

    CompanionPost findById(Long postId); // 게시글 ID로 조회

    @org.springframework.transaction.annotation.Transactional
    void blockPost(Long memberId, Long postId);

    List<CompanionPostListResponseDTO> getBlockedPostList(Long memberId);

}

