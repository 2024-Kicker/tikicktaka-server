
package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CompanionPostService {


    @Transactional
    CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status);
}

