package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.enums.CompanionPostSortType;
import com.example.tikicktaka.domain.enums.CompanionPostStatus;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.images.CompanionPostImg;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.member.ProfileImgRepository;
import com.example.tikicktaka.service.blocked.BlockedService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private BlockedService blockedService;

    @Autowired
    private ScrapRepository scrapRepository;

    @Autowired
    private ProfileImgRepository profileImgRepository;

    @Autowired
    private ScrapCommandService scrapCommandService;

    @Value("${companion.default-images.baseball}")
    private String defaultBaseballImage;

    @Value("${companion.default-images.travel}")
    private String defaultTravelImage;

    @Value("${member.default-profile:}")
    private String defaultProfileImage;


    //게시글 작성
    @Override
    @Transactional
    public CompanionPost createPostWithImages(String title, String content, Long memberId, List<MultipartFile> imageFiles, CompanionPost.PostStatus status, CompanionPost.PostType postType) {
        // Member 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        // 게시글 생성
        CompanionPost post = CompanionPost.builder()
                .title(title)
                .content(content)
                .status(status)
                .postType(postType)
                .author(member)
                .build();

        // 게시글 저장 (우선 저장 후 ID 생성됨)
        companionPostRepository.save(post);

        // 이미지 업로드 및 저장
        List<String> uploaded = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<CompanionPostImg> images = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = utilService.uploadS3Img("companionPost", file);
                    uploaded.add(imageUrl);
                    CompanionPostImg image = CompanionPostImg.builder()
                            .imageUrl(imageUrl)
                            .companionPost(post)
                            .build();

                    images.add(image);
                }
            }
            companionPostImageRepository.saveAll(images);

            if (!images.isEmpty()) {
                //post.setThumbnailUrl(images.get(0).getImageUrl());
                companionPostImageRepository.saveAll(images);
                // 업로드가 있으면 1번을 썸네일로
                post.setThumbnailUrl(uploaded.get(0));
            }
        }
        // 업로드가 없을 때도 DB에 기본 썸네일 저장 (Travel/Baseball 분기)
        if (uploaded.isEmpty()) {
            String fallback = (postType == CompanionPost.PostType.Travel)
                    ? defaultTravelImage
                    : defaultBaseballImage;
            post.setThumbnailUrl(fallback);
        }

        companionPostRepository.save(post); // 변경 사항 저장

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
        ensureThumbnailOrFallback(post);
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        String authorProfile = null;
        if (post.getAuthor() != null) {
            authorProfile = profileImgRepository.findByMember_Id(post.getAuthor().getId())
                    .map(ProfileImg::getUrl)
                    .orElse(defaultProfileImage);
        }
        return CompanionPostResponseDTO.of(post, imageUrls, authorProfile, null, null);
    }

    //게시글 상세 조회
    @Override
    @Transactional(readOnly = true)
    public CompanionPostResponseDTO getPostDetail(Long postId, Long memberId) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (memberId != null && blockedService.isBlocked(memberId, TargetType.COMPANION_POST, postId)) {
            throw new IllegalStateException("차단된 게시글입니다.");
        }
        ensureThumbnailOrFallback(post);
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());

        boolean isScraped = (memberId != null) &&
                scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                        memberId, TargetType.COMPANION_POST, postId);
        boolean isMine = (memberId != null) &&
                post.getAuthor() != null &&
                memberId.equals(post.getAuthor().getId());
        String authorProfile = null;
        if (post.getAuthor() != null) {
            authorProfile = profileImgRepository.findByMember_Id(post.getAuthor().getId())
                    .map(ProfileImg::getUrl)
                    .orElse(defaultProfileImage);
        }

        return CompanionPostResponseDTO.of(post, imageUrls, authorProfile, isScraped, isMine);
    }


    //차단한 게시글 제외하고 게시글 목록 조회 (통합 scrap 기반 isScraped 계산)
    @Override
    @Transactional(readOnly = true)
    public Page<CompanionPostListResponseDTO> getPostList(Long memberId, Pageable pageable) {
        List<Long> blockedPostIds = blockedService.blockedIds(memberId, TargetType.COMPANION_POST);


        Page<CompanionPost> posts;
        if (blockedPostIds.isEmpty()) {
            posts = companionPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            posts = companionPostRepository.findAllByIdNotInOrderByCreatedAtDesc(blockedPostIds, pageable);
        }

        posts.getContent().forEach(this::ensureThumbnailOrFallback);

        // 현재 페이지의 게시글 ID들을 모아놓고, 한 번에 스크랩 여부 set 생성
        List<Long> pagePostIds = posts.getContent().stream()
                .map(CompanionPost::getId)
                .toList();

        Set<Long> scrapedIdSet = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.COMPANION_POST)
                .stream()
                .map(scrap -> scrap.getTargetId())
                .filter(pagePostIds::contains) // 현재 페이지 것만 남김
                .collect(Collectors.toSet());

        return posts.map(post ->
                new CompanionPostListResponseDTO(post, scrapedIdSet.contains(post.getId()),memberId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanionPostListResponseDTO> getPostList(
            Long memberId,
            Pageable pageable,
            CompanionPostSortType sortType,
            CompanionPostStatus statusFilter
    ) {
        // 1) 차단 ID
        List<Long> blockedIds = blockedService.blockedIds(memberId, TargetType.COMPANION_POST);
        boolean hasBlocked = blockedIds != null && !blockedIds.isEmpty();

        // 2) 상태 필터 매핑
        List<CompanionPost.PostStatus> statuses = switch (statusFilter) {
            case FINDING -> List.of(CompanionPost.PostStatus.FINDING);
            case FOUND   -> List.of(CompanionPost.PostStatus.FOUND);
            case ALL     -> List.of(CompanionPost.PostStatus.values());
        };

        // 3) page를 먼저 빈 페이지로 초기화 ★
        Page<CompanionPost> page = Page.empty(pageable);

        // 4) 스크랩 분기
        if (sortType == CompanionPostSortType.SCRAP) {
            List<Long> scrappedIds = scrapRepository
                    .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.COMPANION_POST)
                    .stream()
                    .map(s -> s.getTargetId())
                    .distinct()
                    .toList();

            if (!scrappedIds.isEmpty()) {
                page = hasBlocked
                        ? companionPostRepository.findByIdInAndStatusInAndIdNotIn(scrappedIds, statuses, blockedIds, pageable)
                        : companionPostRepository.findByIdInAndStatusIn(scrappedIds, statuses, pageable);
            }
        } else {
            // LATEST, ALL → 동일: pageable 정렬 사용
            page = hasBlocked
                    ? companionPostRepository.findByStatusInAndIdNotIn(statuses, blockedIds, pageable)
                    : companionPostRepository.findByStatusIn(statuses, pageable);
        }

        page.getContent().forEach(this::ensureThumbnailOrFallback);

        // 5) 현재 페이지 게시글 id들
        List<Long> pagePostIds = page.getContent().stream()
                .map(CompanionPost::getId)
                .toList();

        // 6) 페이지가 비면 스크랩 재조회 생략
        Set<Long> scrapedIdSet = pagePostIds.isEmpty()
                ? java.util.Collections.emptySet()
                : scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.COMPANION_POST)
                .stream()
                .map(s -> s.getTargetId())
                .filter(pagePostIds::contains)
                .collect(Collectors.toSet());

        return page.map(post -> new CompanionPostListResponseDTO(post, scrapedIdSet.contains(post.getId()),memberId));
    }



    //사용자가 차단한 게시글 목록 조회 (통합 scrap 기반 isScraped 계산)
    @Override
    @Transactional(readOnly = true)
    public List<CompanionPostListResponseDTO> getBlockedPostList(Long memberId) {
        List<Long> blockedPostIds = blockedService.blockedIds(memberId, TargetType.COMPANION_POST);
        if (blockedPostIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompanionPost> blockedPosts = companionPostRepository.findAllByIdIn(blockedPostIds);

        // 차단 목록의 게시글들에 대해 스크랩 여부 set 생성
        Set<Long> scrapedIdSet = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.COMPANION_POST)
                .stream()
                .map(scrap -> scrap.getTargetId())
                .collect(Collectors.toSet());

        return blockedPosts.stream()
                .map(post -> new CompanionPostListResponseDTO(post, scrapedIdSet.contains(post.getId()),memberId))
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
        // 존재 검증(선택)
        memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (blockedService.isBlocked(memberId, TargetType.COMPANION_POST, postId)) {
            throw new IllegalStateException("이미 차단한 게시글입니다.");
        }
        blockedService.ensureOn(memberId, TargetType.COMPANION_POST, postId);
    }

    //게시글 차단 해제
    @Override
    @Transactional
    public void unblockPost(Long memberId, Long postId) {
        // 존재 검증(선택)
        memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!blockedService.isBlocked(memberId, TargetType.COMPANION_POST, postId)) {
            throw new IllegalStateException("차단된 게시글이 아닙니다.");
        }
        blockedService.ensureOff(memberId, TargetType.COMPANION_POST, postId);
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

    private void ensureThumbnailOrFallback(CompanionPost post) {
        if (post == null) return;

        if (!StringUtils.hasText(post.getThumbnailUrl())) {
            String fallback = (post.getPostType() == CompanionPost.PostType.Travel)
                    ? defaultTravelImage
                    : defaultBaseballImage; // 기본값은 Baseball
            post.setThumbnailUrl(fallback);
        }
    }

    @Override
    @Transactional
    public CompanionPostResponseDTO updatePostWithImages(
            Long postId,
            Long memberId,
            String title,
            String content,
            CompanionPost.PostStatus status,
            CompanionPost.PostType postType,
            List<MultipartFile> newImages,
            boolean replaceAllImages
    ) {
        CompanionPost post = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (!post.getAuthor().getId().equals(memberId)) {
            throw new IllegalStateException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 1) 부분 필드 업데이트
        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (status != null) post.setStatus(status);
        if (postType != null) post.setPostType(postType);

        // 2) 이미지 처리
        if (replaceAllImages) {
            // 전면 교체: 기존 이미지 전부 삭제(S3/DB)
            List<CompanionPostImg> existing = companionPostImageRepository.findByCompanionPost(post);
            if (!existing.isEmpty()) {
                existing.forEach(img -> utilService.deleteS3Img(img.getImageUrl()));
                companionPostImageRepository.deleteAll(existing);
            }
            post.setThumbnailUrl(null); // 썸네일 초기화
        }

        // APPEND 또는 REPLACE 후 새 이미지 업로드
        List<String> uploaded = new ArrayList<>();
        if (newImages != null && !newImages.isEmpty()) {
            List<CompanionPostImg> addList = new ArrayList<>();
            for (MultipartFile file : newImages) {
                if (file != null && !file.isEmpty()) {
                    String url = utilService.uploadS3Img("companionPost", file);
                    uploaded.add(url);
                    addList.add(CompanionPostImg.builder()
                            .imageUrl(url)
                            .companionPost(post)
                            .build());
                }
            }
            if (!addList.isEmpty()) {
                companionPostImageRepository.saveAll(addList);
                // 썸네일이 비어있다면, 이번 업로드의 첫 이미지를 대표로
                if (!StringUtils.hasText(post.getThumbnailUrl())) {
                    post.setThumbnailUrl(uploaded.get(0));
                }
            }
        }

        // 3) 최종 썸네일 보정(이미지 하나도 없으면 fallback)
        ensureThumbnailOrFallback(post);

        post.preUpdate(); // updatedAt 갱신

        // 4) 응답 DTO
        List<String> imageUrls = companionPostImageRepository.findByCompanionPost(post).stream()
                .map(CompanionPostImg::getImageUrl)
                .collect(Collectors.toList());
        return new CompanionPostResponseDTO(post, imageUrls);
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
