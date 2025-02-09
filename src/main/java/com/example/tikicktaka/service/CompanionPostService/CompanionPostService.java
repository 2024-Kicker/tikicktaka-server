
package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CompanionPostService {


    @Transactional
    CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.TravelStatus travelStatus);

    @Transactional
    CompanionPost deletePost (Long postId, Long memberID); //게시글 삭제 기능
}

