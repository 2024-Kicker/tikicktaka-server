package com.example.tikicktaka.service.storyRoom;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.domain.enums.LimitTime;
import com.example.tikicktaka.domain.enums.StoryRoomStatus;
import com.example.tikicktaka.domain.enums.Topic;
import com.example.tikicktaka.domain.images.StoryRoomImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.*;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomImageRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomParticipantRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomCreateRequestDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomDetailResponseDTO;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomListResponseDTO;
import com.example.tikicktaka.service.UtilService;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.web.dto.storyRoom.StoryRoomPostResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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


    /**
     * 이야기방 게시글 생성 및 이야기방 + 참가자 생성
     */
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
        participant.setStoryRoomId(room.getId());
        participant.setMemberId(author.getId());
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


    @Override
    @Transactional
    public Long createStoryRoom(StoryRoomCreateRequestDTO request, Long creatorId) {
        Member creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        StoryRoomPost post = new StoryRoomPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTopic(request.getTopic());
        post.setLimitTime(request.getLimitTime());
        post.setAuthor(creator);
        post.setCreatedAt(LocalDateTime.now());
        post.setEnterableUntil(LocalDateTime.now().plusMinutes(20));
        post.setStatus(StoryRoomStatus.IN_PROGRESS);

        storyRoomPostRepository.saveAndFlush(post);  // post ID 확보

        String roomId = "room-" + java.util.UUID.randomUUID();  // 고유 채팅방 ID 생성

        StoryRoom room = new StoryRoom(
                request.getTitle(),
                request.getContent(),
                creator,
                post,
                roomId
        );

        storyRoomRepository.save(room);

        return room.getId();
    }

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

        if (!post.getAuthor() .getId().equals(memberId)) {
            return ApiResponse.onFailure(
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getCode(),
                    ErrorStatus.STORYROOMPOST_NOT_OWNER.getMessage(),
                    null
            );
            }

        storyRoomRepository.deleteByPost(post);
        storyRoomPostRepository.delete(post);

        return ApiResponse.onSuccess("게시글과 채팅방이 삭제되었습니다.");
    }


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




    @Override
    public StoryRoomDetailResponseDTO getStoryRoomDetail(Long id) {
        StoryRoom room = storyRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));
        return new StoryRoomDetailResponseDTO(room);
    }

    @Override
    public List<StoryRoomListResponseDTO> getAllStoryRooms() {
        return storyRoomRepository.findAll().stream()
                .map(StoryRoomListResponseDTO::new)
                .collect(Collectors.toList());
    }
}
