package com.example.tikicktaka.service.storyRoom;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.domain.enums.*;
import com.example.tikicktaka.domain.images.StoryRoomImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.*;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.scrap.ScrapRepository;
import com.example.tikicktaka.repository.storyRoom.*;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.service.blocked.BlockedService;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomPostResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryRoomServiceImpl implements StoryRoomService {

    private final StoryRoomRepository storyRoomRepository;
    private final MemberRepository memberRepository;
    private final StoryRoomParticipantRepository storyRoomParticipantRepository;
    private final StoryRoomPostRepository storyRoomPostRepository;
    private final UtilService utilService;
    private final StoryRoomImageRepository storyRoomImageRepository;
    private final ScrapRepository scrapRepository;

    /** ✅ 통합 차단 서비스 */
    private final BlockedService blockedService;

    // 이야기방 게시글 생성 및 이야기방 + 참가자 생성
    @Override
    @Transactional
    public StoryRoomPostResponseDTO createStoryRoomPost(String title, String content, Topic topic, LimitTime limitTime,
                                                        List<MultipartFile> imageFiles, Long authorId) {

        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 게시글 생성
        StoryRoomPost post = new StoryRoomPost();
        post.setTitle(title);
        post.setContent(content);
        post.setTopic(topic);
        post.setLimitTime(limitTime);
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setEnterableUntil(LocalDateTime.now().plusMinutes(20));
        post.setStatus(StoryRoomStatus.IN_PROGRESS);
        storyRoomPostRepository.saveAndFlush(post);  // ID 확보를 위해 flush

        // 2. 이야기방 생성
        StoryRoom room = new StoryRoom(post, author);
        storyRoomRepository.save(room);

        // 3. 참가자 등록(방장)
        StoryRoomParticipant participant = new StoryRoomParticipant();
        participant.setStoryRoom(room);
        participant.setStoryRoomPost(post);
        participant.setMember(author);
        participant.setRole(StoryRoomParticipant.Role.OWNER);
        storyRoomParticipantRepository.save(participant);

        // 4. 이미지 업로드 및 저장
        List<String> imageUrls = new ArrayList<>();
        List<StoryRoomImg> storyRoomImgs = new ArrayList<>();

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = utilService.uploadS3Img("storyRoom", file);
                    imageUrls.add(imageUrl);

                    StoryRoomImg image = StoryRoomImg.builder()
                            .imageUrl(imageUrl)
                            .storyRoomPost(post)
                            .build();
                    storyRoomImgs.add(image);
                }
            }

            if (!storyRoomImgs.isEmpty()) {
                storyRoomImageRepository.saveAll(storyRoomImgs);
                post.setThumbnailUrl(imageUrls.get(0));  // 첫 번째 이미지 썸네일로 설정
            }
        }

        return new StoryRoomPostResponseDTO(post, imageUrls, 1);
    }

    // 채팅방 게시글 삭제
    @Transactional
    public ApiResponse<?> deleteStoryRoomPost(Long storyRoomPostId, Long memberId) {
        Optional<StoryRoomPost> optionalPost = storyRoomPostRepository.findById(storyRoomPostId);
        if (optionalPost.isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getMessage(),
                    null
            );
        }

        StoryRoomPost post = optionalPost.get();
        if (!post.getAuthor().getId().equals(memberId)) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getMessage(),
                    null
            );
        }

        List<StoryRoomImg> images = storyRoomImageRepository.findByStoryRoomPost(post);
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> utilService.deleteS3Img(image.getImageUrl()));
            storyRoomImageRepository.deleteAll(images);
        }

        storyRoomRepository.deleteByPost(post);
        storyRoomPostRepository.delete(post);

        return ApiResponse.onSuccess("게시글과 채팅방이 삭제되었습니다.");
    }

    // 비로그인한 사용자 용 게시글 상세 조회
    @Override
    public StoryRoomPostResponseDTO getStoryRoomPostDetail(Long postId) {
        StoryRoomPost post = storyRoomPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        List<String> imageUrls = storyRoomImageRepository.findByStoryRoomPost(post).stream()
                .map(StoryRoomImg::getImageUrl)
                .collect(Collectors.toList());

        StoryRoom room = storyRoomRepository.findByPost(post)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글과 연결된 이야기방이 없습니다."));

        int participantCount = storyRoomParticipantRepository.countByStoryRoomId(room.getId());

        return new StoryRoomPostResponseDTO(post, imageUrls, participantCount);
    }

    // 로그인한 사용자용 게시글 상세 조회 (통합 차단 체크 포함)
    @Override
    public StoryRoomPostResponseDTO getStoryRoomPostDetail(Long postId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        StoryRoomPost post = storyRoomPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // ✅ 통합 차단(신고) 체크
        if (blockedService.isBlocked(memberId, TargetType.STORY_POST, postId)) {
            throw new IllegalStateException("신고한 게시글입니다.");
        }

        List<String> imageUrls = storyRoomImageRepository.findByStoryRoomPost(post).stream()
                .map(StoryRoomImg::getImageUrl)
                .collect(Collectors.toList());

        StoryRoom room = (StoryRoom) storyRoomRepository.findByPost(post)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글과 연결된 이야기방이 없습니다."));
        int participantCount = storyRoomParticipantRepository.countByStoryRoomId(room.getId());

        boolean isScrapped = scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.STORY_POST, postId);

        return new StoryRoomPostResponseDTO(post, imageUrls, participantCount, isScrapped);
    }

    // 게시글 스크랩
    @Override
    @Transactional
    public void scrap(Long memberId, Long postId) {
        if (!storyRoomPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, TargetType.STORY_POST, postId)) {
            return; // 멱등
        }
        scrapRepository.save(com.example.tikicktaka.domain.mapping.scrap.Scrap.of(
                memberId, TargetType.STORY_POST, postId
        ));
    }

    // 게시글 스크랩 해제 (통합 scrap)
    @Override
    @Transactional
    public void unscrap(Long memberId, Long postId) {
        if (!storyRoomPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }
        scrapRepository.deleteByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.STORY_POST, postId
        );
    }

    // 특정 사용자가 특정 게시글을 스크랩했는지? (통합 scrap)
    @Override
    public boolean isScrapped(Long memberId, Long postId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.");
        }
        if (!storyRoomPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }
        return scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.STORY_POST, postId
        );
    }

    // 스크랩된 게시글 목록 확인 (통합 scrap)
    @Override
    @Transactional
    public List<StoryRoomPostResponseDTO> getScrappedPosts(Long memberId) {
        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.STORY_POST)
                .stream()
                .map(com.example.tikicktaka.domain.mapping.scrap.Scrap::getTargetId)
                .toList();

        if (ids.isEmpty()) return List.of();

        Map<Long, StoryRoomPost> map = storyRoomPostRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(StoryRoomPost::getId, it -> it));

        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(post -> StoryRoomPostResponseDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .isScrapped(true)
                        .participantCount(post.getParticipants().size())
                        .build())
                .collect(Collectors.toList());
    }

    // 필터 게시글 목록 반환 (로그인 사용자 기반 스크랩 포함) — 통합 차단 반영
    public List<StoryRoomPostResponseDTO> getFilteredStoryRoomPosts(StoryRoomStatus status,
                                                                    StoryRoomPostSortType sortType,
                                                                    Topic topic,
                                                                    Long memberId) {

        // ✅ 통합 차단 id
        List<Long> blockedIds = (memberId == null)
                ? List.of()
                : blockedService.blockedIds(memberId, TargetType.STORY_POST);

        // 모든 게시글
        List<StoryRoomPost> posts = storyRoomPostRepository.findAll();

        // 차단 제외
        if (!blockedIds.isEmpty()) {
            posts = posts.stream()
                    .filter(post -> !blockedIds.contains(post.getId()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            posts = posts.stream()
                    .filter(post -> post.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (topic != null) {
            posts = posts.stream()
                    .filter(post -> post.getTopic() == topic)
                    .collect(Collectors.toList());
        }

        if (sortType != null) {
            if (sortType == StoryRoomPostSortType.LATEST) {
                posts.sort(Comparator.comparing(StoryRoomPost::getCreatedAt).reversed());
            } else if (sortType == StoryRoomPostSortType.SCRAP) {
                posts = posts.stream()
                        .filter(post -> memberId != null &&
                                scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                                        memberId, TargetType.STORY_POST, post.getId()))
                        .collect(Collectors.toList());
            }
        }

        return posts.stream()
                .map(post -> {
                    boolean isScrapped = memberId != null &&
                            scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                                    memberId, TargetType.STORY_POST, post.getId());
                    int participantCount = post.getParticipants().size();
                    return new StoryRoomPostResponseDTO(post, null, participantCount, isScrapped);
                })
                .collect(Collectors.toList());
    }

    // 게시글 신고하기(=차단)
    @Override
    @Transactional
    public ApiResponse<String> blockStoryRoomPost(Long memberId, Long postId) {
        if (memberRepository.findById(memberId).isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.MEMBER_NOT_FOUND.getCode(),
                    ErrorStatus.MEMBER_NOT_FOUND.getMessage(),
                    null
            );
        }

        if (storyRoomPostRepository.findById(postId).isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getMessage(),
                    null
            );
        }

        if (blockedService.isBlocked(memberId, TargetType.STORY_POST, postId)) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_ALREADY_BLOCKED.getCode(),
                    ErrorStatus.STORYROOMPOST_ALREADY_BLOCKED.getMessage(),
                    null
            );
        }

        blockedService.ensureOn(memberId, TargetType.STORY_POST, postId);
        return ApiResponse.onSuccess("게시글이 신고되었습니다.");
    }
}
