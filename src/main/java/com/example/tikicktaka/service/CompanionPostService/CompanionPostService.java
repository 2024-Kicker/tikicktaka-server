
package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.enums.CompanionPostSortType;
import com.example.tikicktaka.domain.enums.CompanionPostStatus;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CompanionPostService {
    @Transactional
    CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.PostType postType);

    @Transactional
    CompanionPost deletePost (Long postId, Long memberID); //게시글 삭제 기능
    CompanionPostResponseDTO getPostDetail(Long postId); //게시글 상세 조회

    @Transactional
    CompanionPostResponseDTO getPostDetail(Long postId, Long memberId);

    //차단한 게시글 제외하고 게시글 목록 조회 (통합 scrap 기반 isScraped 계산)
    @Transactional
    Page<CompanionPostListResponseDTO> getPostList(Long memberId, Pageable pageable);

    @Transactional
    Page<CompanionPostListResponseDTO> getPostList(
            Long memberId,
            Pageable pageable,
            CompanionPostSortType sortType,
            CompanionPostStatus statusFilter
    );

    CompanionPostResponseDTO updatePostWithImages(
            Long postId,
            Long memberId,
            String title,
            String content,
            CompanionPost.PostStatus status,
            CompanionPost.PostType postType,
            List<MultipartFile> newImages,
            boolean replaceAllImages
    );


    CompanionPost findById(Long postId); // 게시글 ID로 조회

    @Transactional
    void blockPost(Long memberId, Long postId);
    void unblockPost(Long memberId, Long postId);

    List<CompanionPostListResponseDTO> getBlockedPostList(Long memberId);

    void updatePostStatus(Long postId, CompanionPost.PostStatus status, Long memberId);


    void scrapPost(Long memberId, Long postId);

    void unScrapPost(Long memberId, Long postId);
}

