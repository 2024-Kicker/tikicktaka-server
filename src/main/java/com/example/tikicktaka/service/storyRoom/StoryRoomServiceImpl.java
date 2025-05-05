package com.example.tikicktaka.service.storyRoom;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.StoryRoomPostSortType;
import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.domain.images.StoryRoomImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.*;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.storyRoom.*;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomPostResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    private final ScrapedStoryRoomRepository scrapedStoryRoomRepository;
    private final BlockedStoryRoomPostRepository blockedStoryRoomPostRepository;



     //이야기방 게시글 생성 및 이야기방 + 참가자 생성
    @Override
    @Transactional
    public StoryRoomPostResponseDTO createStoryRoomPost(String title, String content, Topic topic, LimitTime limitTime, List<MultipartFile> imageFiles, Long authorId) {

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

        // 3. 참가자 등록
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

        // StoryRoomPostResponseDTO 반환
        return new StoryRoomPostResponseDTO(post, imageUrls,1);
    }


//    //이야기방 채팅방 만들기
//    @Override
//    @Transactional
//    public Long createStoryRoom(StoryRoomCreateRequestDTO request, Long creatorId) {
//        Member creator = memberRepository.findById(creatorId)
//                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
//
//        StoryRoomPost post = new StoryRoomPost();
//        post.setTitle(request.getTitle());
//        post.setContent(request.getContent());
//        post.setTopic(request.getTopic());
//        post.setLimitTime(request.getLimitTime());
//        post.setAuthor(creator);
//        post.setCreatedAt(LocalDateTime.now());
//        post.setEnterableUntil(LocalDateTime.now().plusMinutes(20));
//        post.setStatus(StoryRoomStatus.IN_PROGRESS);
//
//        storyRoomPostRepository.saveAndFlush(post);  // post ID 확보
//
//        String roomId = "room-" + java.util.UUID.randomUUID();  // 고유 채팅방 ID 생성
//
//        StoryRoom room = new StoryRoom(
//                request.getTitle(),
//                request.getContent(),
//                creator,
//                post,
//                roomId
//        );
//
//        storyRoomRepository.save(room);
//
//        return room.getId();
//    }


    //채팅방 게시글 삭제
    @Transactional
    public ApiResponse<?> deleteStoryRoomPost(Long storyRoomPostId, Long memberId) {
        Optional<StoryRoomPost> optionalPost = storyRoomPostRepository.findById(storyRoomPostId);
        //게시글이 있는지 확인
        if (optionalPost.isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getMessage(),
                    null
            );
        }

        StoryRoomPost post = optionalPost.get();
        //작성자 본인인지 확인
        if (!post.getAuthor() .getId().equals(memberId)) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getMessage(),
                    null
            );
        }
        // 게시글에 연결된 이미지 리스트 조회
        List<StoryRoomImg> images = storyRoomImageRepository.findByStoryRoomPost(post);

        if (images != null && !images.isEmpty()) {
            //S3에서 이미지 삭제
            images.forEach(image -> utilService.deleteS3Img(image.getImageUrl()));

            //DB에서 이미지 삭제
            storyRoomImageRepository.deleteAll(images);
        }

        storyRoomRepository.deleteByPost(post);
        storyRoomPostRepository.delete(post);

        return ApiResponse.onSuccess("게시글과 채팅방이 삭제되었습니다.");
    }


    //비로그인한 사용자 용 게시글 상세 조회
    @Override
    public StoryRoomPostResponseDTO getStoryRoomPostDetail(Long postId) {
        StoryRoomPost post = storyRoomPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        List<String> imageUrls = storyRoomImageRepository.findByStoryRoomPost(post).stream()
                .map(StoryRoomImg::getImageUrl)
                .collect(Collectors.toList());

        StoryRoom room = (StoryRoom) storyRoomRepository.findByPost(post)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글과 연결된 이야기방이 없습니다."));

        int participantCount = storyRoomParticipantRepository.countByStoryRoomId(room.getId());

        return new StoryRoomPostResponseDTO(post, imageUrls, participantCount);
    }

    //로그인한 사용자용 게시글 상세 조회
    @Override
    public StoryRoomPostResponseDTO getStoryRoomPostDetail(Long postId, Long memberId) {
        // 로그인된 사용자 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        // 게시글 조회
        StoryRoomPost post = storyRoomPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (memberId != null && blockedStoryRoomPostRepository.existsByMemberIdAndStoryRoomPostId(memberId, postId)) {
            throw new IllegalStateException("신고한 게시글입니다.");
        }

        // 이미지 리스트 조회
        List<String> imageUrls = storyRoomImageRepository.findByStoryRoomPost(post).stream()
                .map(StoryRoomImg::getImageUrl)
                .collect(Collectors.toList());

        // 이야기방 조회 및 참여 인원 수 확인
        StoryRoom room = (StoryRoom) storyRoomRepository.findByPost(post)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글과 연결된 이야기방이 없습니다."));

        int participantCount = storyRoomParticipantRepository.countByStoryRoomId(room.getId());
        boolean isScrapped = scrapedStoryRoomRepository.existsByMemberIdAndStoryRoomPostId(memberId, postId);
        System.out.println("Member ID: " + memberId + ", Post ID: " + postId + ", Is Scrapped: " + isScrapped);

        // 응답 DTO 생성
        return new StoryRoomPostResponseDTO(post, imageUrls, participantCount, isScrapped);
    }


//    //이야기 방 상세조회
//    @Override
//    public StoryRoomDetailResponseDTO getStoryRoomDetail(Long id) {
//        StoryRoom room = storyRoomRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));
//        return new StoryRoomDetailResponseDTO(room);
//    }

//    //이야기방 목록 조회
//    @Override
//    public List<StoryRoomListResponseDTO> getAllStoryRooms() {
//        return storyRoomRepository.findAll().stream()
//                .map(StoryRoomListResponseDTO::new)
//                .collect(Collectors.toList());
//    }

    //게시글 스크랩
    @Override
    public void scrap(Long memberId, Long postId) {
        if (scrapedStoryRoomRepository.existsByMemberIdAndStoryRoomPostId(memberId, postId)) {
            return; // 이미 스크랩된 경우 중복 저장 방지
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
        StoryRoomPost post = storyRoomPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));

        ScrapedStoryRoomPost scrap = new ScrapedStoryRoomPost();
        scrap.setMember(member);
        scrap.setStoryRoomPost(post);
        scrap.setCreatedAt(LocalDateTime.now());

        scrapedStoryRoomRepository.save(scrap);
    }

    //게시글 스크랩 해제
    @Override
    public void unscrap(Long memberId, Long postId) {
        // 게시글 존재 여부 확인
        if (!storyRoomPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }

        // 스크랩 정보 조회 및 삭제
        ScrapedStoryRoomPost scrap = scrapedStoryRoomRepository.findByMemberIdAndStoryRoomPostId(memberId, postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스크랩 정보를 찾을 수 없습니다."));

        scrapedStoryRoomRepository.delete(scrap);
    }


    //특정 사용자가 특정 게시글을 스크랩했는지?
    @Override
    public boolean isScrapped(Long memberId, Long postId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.");
        }
        if (!storyRoomPostRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 게시글을 찾을 수 없습니다.");
        }
        return scrapedStoryRoomRepository.existsByMemberIdAndStoryRoomPostId(memberId, postId);
    }

    //스크랩된 게시글 목록 확인
    @Override
    public List<StoryRoomPostResponseDTO> getScrappedPosts(Long memberId) {
        List<ScrapedStoryRoomPost> scraps = scrapedStoryRoomRepository.findAllByMemberId(memberId);

        return scraps.stream()
                .map(scrap -> {
                    StoryRoomPost post = scrap.getStoryRoomPost();
                    return StoryRoomPostResponseDTO.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .createdAt(post.getCreatedAt())
                            .isScrapped(true)
                            .participantCount(post.getParticipants().size())
                            .build();
                })
                .collect(Collectors.toList());

    }

    // 필터 게시글 목록 반환 (로그인 사용자 기반 스크랩 포함)
    public List<StoryRoomPostResponseDTO> getFilteredStoryRoomPosts(StoryRoomStatus status, StoryRoomPostSortType sortType, Topic topic, Long memberId) {

        //List<StoryRoomPost> posts = storyRoomPostRepository.findAll();
        List<Long> blockedIds = blockedStoryRoomPostRepository.findBlockedPostIdsByMemberId(memberId); // 차단된 게시글 조회

        // 모든 게시글을 가져옵니다.
        List<StoryRoomPost> posts = storyRoomPostRepository.findAll();

        // 차단된 게시글을 제외합니다.
        if (!blockedIds.isEmpty()) {
            posts = posts.stream()
                    .filter(post -> !blockedIds.contains(post.getId())) // 차단된 게시글 제외
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
                        .filter(post -> scrapedStoryRoomRepository.existsByMemberIdAndStoryRoomPostId(memberId, post.getId()))
                        .collect(Collectors.toList());
            }
        }

        return posts.stream()
                .map(post -> {
                    boolean isScrapped = scrapedStoryRoomRepository.existsByMemberIdAndStoryRoomPostId(memberId, post.getId());
                    int participantCount = post.getParticipants().size();
                    return new StoryRoomPostResponseDTO(post, null, participantCount, isScrapped);
                })
                .collect(Collectors.toList());
    }

    //게시글 신고하기(=차단)
    @Override
    @Transactional
    public ApiResponse<String> blockStoryRoomPost(Long memberId, Long postId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.MEMBER_NOT_FOUND.getCode(),
                    ErrorStatus.MEMBER_NOT_FOUND.getMessage(),
                    null
            );
        }

        Optional<StoryRoomPost> optionalPost = storyRoomPostRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_FOUND.getMessage(),
                    null
            );
        }

        boolean alreadyBlocked = blockedStoryRoomPostRepository.existsByMemberIdAndStoryRoomPostId(memberId, postId);
        if (alreadyBlocked) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_ALREADY_BLOCKED.getCode(),
                    ErrorStatus.STORYROOMPOST_ALREADY_BLOCKED.getMessage(),
                    null
            );
        }

        BlockedStoryRoomPost blocked = new BlockedStoryRoomPost(optionalMember.get(), optionalPost.get());
        blockedStoryRoomPostRepository.save(blocked);

        return ApiResponse.onSuccess("게시글이 신고되었습니다.");
    }




}
