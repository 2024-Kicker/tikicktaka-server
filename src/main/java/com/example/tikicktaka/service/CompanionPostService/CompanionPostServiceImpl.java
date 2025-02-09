package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostImageRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.UtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class CompanionPostServiceImpl implements CompanionPostService {

    @Autowired
    private CompanionPostRepository companionPostRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UtilService utilService;

    @Autowired
    private CompanionPostImageRepository  companionPostImageRepository;

    @Override
    @Transactional
    public CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status) {
        // Member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        // 게시글 생성
        CompanionPost post = CompanionPost.builder()
                .title(title)
                .content(content)
                .status(status)
                .author(member)
                .build();

        // 게시글 저장 (우선 저장 후 ID 생성됨)
        companionPostRepository.save(post);

        // 이미지 업로드 및 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<CompanionPostImg> images = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    // UtilService를 사용하여 S3에 이미지 업로드
                    String imageUrl = utilService.uploadS3Img("companionPost", file);

                    // CompanionPostImage 객체 생성
                    CompanionPostImg image = CompanionPostImg.builder()
                            .imageUrl(imageUrl)
                            .companionPost(post)
                            .build();

                    images.add(image);
                }
            }
            // 이미지 저장
            companionPostImageRepository.saveAll(images);

            // ✅ 첫 번째 이미지를 대표 이미지(썸네일)로 설정
            if (!images.isEmpty()) {
                post.setThumbnailUrl(images.get(0).getImageUrl());
            }
        }

        return post;
    }

}

