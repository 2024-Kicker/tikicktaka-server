package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.BlockedPost;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.companionPost.ScrapedPost;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPost.BlockedPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostImageRepository;
import com.example.tikicktaka.repository.companionPost.ScrapedPostRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.service.chatService.InviteCodeGeneratorService;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ChatRoomService chatRoomService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private CompanionPostImageRepository  companionPostImageRepository;

    @Autowired
    private InviteCodeGeneratorService InviteCodeGenerator;

    @Autowired
    private BlockedPostRepository blockedPostRepository;
    @Autowired
    private ScrapedPostRepository scrapedPostRepository;


    //게시글 작성
    @Override
    @Transactional
    public CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.TravelStatus travelStatus) {
        // Member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        // 게시글 생성
        CompanionPost post = CompanionPost.builder()
                .title(title)
                .content(content)
                .status(status)
                .travelStatus(travelStatus)
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

            // 첫 번째 이미지를 대표 이미지(썸네일)로 설정
            if (!images.isEmpty()) {
                post.setThumbnailUrl(images.get(0).getImageUrl());
            }
        }

        return post;
    }

    @Override
    @Transactional
    public CompanionPost deletePost(Long postId, Long memberId) {
        //게시글 조회
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 본인 게시글인지 확인
        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 게시글에 연결된 이미지 리스트 조회
        List<CompanionPostImg> images = companionPostImageRepository.findByCompanionPost(post);

        if (images != null && !images.isEmpty()) {
            //S3에서 이미지 삭제
            images.forEach(image -> utilService.deleteS3Img(image.getImageUrl()));

            //DB에서 이미지 삭제
            companionPostImageRepository.deleteAll(images);
        }

        //게시글 삭제하면 채팅방도 삭제되게
        chatRoomService.deleteRoomsByPostId(postId);
        //DB에서 게시글 삭제
        companionPostRepository.delete(post);

        return post;
    }


    //공유용 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public CompanionPostResponseDTO getPostDetail(Long postId){
        //게시글 조회
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        return new CompanionPostResponseDTO(post, imageUrls);
    }


    //게시글 상세 조회
    @Override
    @Transactional(readOnly = true)
    public CompanionPostResponseDTO getPostDetail(Long postId, Long memberId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (memberId != null && blockedPostRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw new IllegalStateException("차단된 게시글입니다.");
        }

        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        return new CompanionPostResponseDTO(post, imageUrls);
    }


    //차단한 게시글 제외하고 게시글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<CompanionPostListResponseDTO> getPostList(Long memberId, Pageable pageable) {
        List<Long> blockedPostIds = blockedPostRepository.findPostIdsByMemberId(memberId);

        Page<CompanionPost> posts;
        if (blockedPostIds.isEmpty()) {
            // 차단한 게시글이 없으면 전체 조회
            posts = companionPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            // 차단한 게시글이 있으면 제외하고 조회
            posts = companionPostRepository.findAllByIdNotInOrderByCreatedAtDesc(blockedPostIds, pageable);
        }

        //return posts.map(CompanionPostListResponseDTO::new);
        // DTO 변환 (isScraped 값을 추가하여 변환)
        return posts.map(post -> {
            boolean isScraped = scrapedPostRepository.existsByMemberIdAndCompanionPostId(memberId, post.getId());
            return new CompanionPostListResponseDTO(post, isScraped);
        });
    }


    //사용자가 차단한 게시글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CompanionPostListResponseDTO> getBlockedPostList(Long memberId) {
        // memberId에 해당하는 차단된 게시글 목록을 조회
        List<Long> blockedPostIds = blockedPostRepository.findPostIdsByMemberId(memberId);

        if (blockedPostIds.isEmpty()) {
            return new ArrayList<>();  // 차단된 게시글이 없으면 빈 리스트 반환
        }

        // 차단된 게시글 IDs를 기반으로 게시글들을 조회
        List<CompanionPost> blockedPosts = companionPostRepository.findAllByIdIn(blockedPostIds);

        return blockedPosts.stream()
                .map(post -> {
                    boolean isScraped = scrapedPostRepository.existsByMemberIdAndCompanionPostId(memberId, post.getId());
                    return new CompanionPostListResponseDTO(post, isScraped);
                })
                .collect(Collectors.toList());

    }




    // 게시글 ID로 조회하는 메서드 추가
    @Override
    @Transactional(readOnly = true)
    public CompanionPost findById(Long postId) {
        return companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));
    }

    //게시글 차단
    @Override
    @Transactional
    public void blockPost(Long memberId, Long postId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 이미 차단했는지 확인
        if (blockedPostRepository.existsByMemberAndBlockedPost(member, post)) {
            throw new IllegalStateException("이미 차단한 게시글입니다.");
        }

        // 차단 저장
        BlockedPost blockedPost = BlockedPost.builder()
                .member(member)
                .blockedPost(post)
                .build();
        blockedPostRepository.save(blockedPost);
    }


    //게시글 차단 해제
    @Override
    @Transactional
    public void unblockPost(Long memberId, Long postId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 차단 이력 조회
        BlockedPost blockedPost = blockedPostRepository.findByMemberAndBlockedPost(member, post)
                .orElseThrow(() -> new IllegalStateException("차단된 게시글이 아닙니다."));

        // 차단 해제
        blockedPostRepository.delete(blockedPost);
    }

    @Override
    @Transactional
    public void updatePostStatus(Long postId, CompanionPost.PostStatus status, Long memberId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.setStatus(status);
        post.preUpdate(); // updatedAt을 갱신해주기
    }

    @Transactional
    public void scrapPost(Long memberId, Long postId) {
        // 이미 스크랩한 게시글인지 확인
        if (scrapedPostRepository.existsByMemberIdAndCompanionPostId(memberId, postId)) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다.");
        }

        // 스크랩 저장
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        ScrapedPost scrap = ScrapedPost.builder()
                .member(member)
                .companionPost(post)
                .build();
        scrapedPostRepository.save(scrap);
    }

    @Transactional
    public void unScrapPost(Long memberId, Long postId) {

        // 게시글 존재 여부 확인
        if (!companionPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }

        // 스크랩 정보 조회 및 삭제
        ScrapedPost scrap = (ScrapedPost) scrapedPostRepository.findByMemberIdAndCompanionPostId(memberId, postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스크랩 정보를 찾을 수 없습니다."));

        scrapedPostRepository.delete(scrap);
    }



}

