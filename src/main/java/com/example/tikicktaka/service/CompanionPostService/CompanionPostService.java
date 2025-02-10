
package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CompanionPostService {


    @Transactional
    CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.TravelStatus travelStatus);

    @Transactional
    CompanionPost deletePost (Long postId, Long memberID); //게시글 삭제 기능

    Page<CompanionPostListResponseDTO> getPostList(Pageable pageable); //게시글 목록 조회
}

