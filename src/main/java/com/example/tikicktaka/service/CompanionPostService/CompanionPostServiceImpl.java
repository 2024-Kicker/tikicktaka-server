package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.BlockedPost;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.enums.ScrapTargetType;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPost.BlockedPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostImageRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.scrap.ScrapRepository;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.service.chatService.InviteCodeGeneratorService;
import com.example.tikicktaka.service.scrap.ScrapCommandService;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostListResponseDTO;
import com.example.tikicktaka.web.dto.companionPost.CompanionPostResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    // ✅ 통합 scrap 사용
    @Autowired
    private ScrapRepository scrapRepository;

    @Autowired
    private ScrapCommandService scrapCommandService;

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
                    String imageUrl = utilService.uploadS3Img("companionPost", file);

                    CompanionPostImg image = CompanionPostImg.builder()
                            .imageUrl(imageUrl)
                            .companionPost(post)
                            .build();

                    images.add(image);
                }
            }
            companionPostImageRepository.saveAll(images);

            if (!images.isEmpty()) {
                post.setThumbnailUrl(images.get(0).getImageUrl());
            }
        }

        return post;
    }

    @Override
    @Transactional
    public CompanionPost deletePost(Long postId, Long memberId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        List<CompanionPostImg> images = companionPostImageRepository.findByCompanionPost(post);

        if (images != null && !images.isEmpty()) {
            images.forEach(image -> utilService.deleteS3Img(image.getImageUrl()));
            companionPostImageRepository.deleteAll(images);
        }

        chatRoomService.deleteRoomsByPostId(postId);
        companionPostRepository.delete(post);

        return post;
    }

    //공유용 게시글 조회
    @Override
    @Transactional(readOnly = true)
    public CompanionPostResponseDTO getPostDetail(Long postId){
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

    //차단한 게시글 제외하고 게시글 목록 조회 (통합 scrap 기반 isScraped 계산)
    @Override
    @Transactional(readOnly = true)
    public Page<CompanionPostListResponseDTO> getPostList(Long memberId, Pageable pageable) {
        List<Long> blockedPostIds = blockedPostRepository.findPostIdsByMemberId(memberId);

        Page<CompanionPost> posts;
        if (blockedPostIds.isEmpty()) {
            posts = companionPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = companionPostRepository.findAllByIdNotInOrderByCreatedAtDesc(blockedPostIds, pageable);
        }

        // 현재 페이지의 게시글 ID들을 모아놓고, 한 번에 스크랩 여부 set 생성
        List<Long> pagePostIds = posts.getContent().stream()
                .map(CompanionPost::getId)
                .toList();

        Set<Long> scrapedIdSet = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, ScrapTargetType.COMPANION_POST)
                .stream()
                .map(scrap -> scrap.getTargetId())
                .filter(pagePostIds::contains) // 현재 페이지 것만 남김
                .collect(Collectors.toSet());

        return posts.map(post ->
                new CompanionPostListResponseDTO(post, scrapedIdSet.contains(post.getId()))
        );
    }

    //사용자가 차단한 게시글 목록 조회 (통합 scrap 기반 isScraped 계산)
    @Override
    @Transactional(readOnly = true)
    public List<CompanionPostListResponseDTO> getBlockedPostList(Long memberId) {
        List<Long> blockedPostIds = blockedPostRepository.findPostIdsByMemberId(memberId);
        if (blockedPostIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompanionPost> blockedPosts = companionPostRepository.findAllByIdIn(blockedPostIds);

        // 차단 목록의 게시글들에 대해 스크랩 여부 set 생성
        Set<Long> scrapedIdSet = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, ScrapTargetType.COMPANION_POST)
                .stream()
                .map(scrap -> scrap.getTargetId())
                .collect(Collectors.toSet());

        return blockedPosts.stream()
                .map(post -> new CompanionPostListResponseDTO(post, scrapedIdSet.contains(post.getId())))
                .collect(Collectors.toList());
    }

    // 게시글 ID로 조회
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

        if (blockedPostRepository.existsByMemberAndBlockedPost(member, post)) {
            throw new IllegalStateException("이미 차단한 게시글입니다.");
        }

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

        BlockedPost blockedPost = blockedPostRepository.findByMemberAndBlockedPost(member, post)
                .orElseThrow(() -> new IllegalStateException("차단된 게시글이 아닙니다."));

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
        post.preUpdate(); // updatedAt 갱신
    }

    // ===== 통합 scrap 기반 추가/해제 위임 =====
    @Override
    @Transactional
    public void scrapPost(Long memberId, Long postId) {
        scrapCommandService.addCompanionPost(memberId, postId);
    }

    @Override
    @Transactional
    public void unScrapPost(Long memberId, Long postId) {
        scrapCommandService.removeCompanionPost(memberId, postId);
    }
}
