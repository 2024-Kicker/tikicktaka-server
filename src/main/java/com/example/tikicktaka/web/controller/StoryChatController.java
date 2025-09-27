package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomParticipantRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.service.storyChat.StoryChatMessageService;
import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/story/chats")
@Tag(name = "StoryRoom Chat", description = "이야기방 채팅방 api")

public class StoryChatController {

    private final StoryRoomRepository storyRoomRepository;
    private final StoryRoomParticipantRepository participantRepository;
    private final StoryChatMessageService storyChatMessageService;
    private final MemberRepository memberRepository;

    private String stripSrPrefix(String id) {
        if (id == null) return null;
        return id.startsWith("SR-") ? id.substring(3) : id;
    }


    // 1) GET /posts/{postId}/room  -> roomId 반환
    @GetMapping("/posts/{postId}/room")
    @Operation(summary = "roomId 반환", description = "postID 기반으로 해당 RoomID를 반환합니다")
    public ApiResponse<String> getRoomIdByPost(@PathVariable Long postId) {
        String roomId = storyRoomRepository.findByPostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("이야기방이 존재하지 않습니다."))
                .getRoomId();
        return ApiResponse.onSuccess("SR-" + roomId); // SR- 접두사 부여
    }

    // 2) GET /rooms/{roomId}/participants/count -> 참가자 수
    @GetMapping("/rooms/{roomId}/participants/count")
    @Operation(summary = "참가자 수 반환 api", description = "채팅방의 참가 인원수를 반환합니다")
    public ApiResponse<Integer> countParticipants(@PathVariable String roomId) {
        StoryRoom room = storyRoomRepository.findByRoomId(stripSrPrefix(roomId)) // 접두사 제거 후 조회
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));
        int count = participantRepository.countByStoryRoomId(room.getId());
        return ApiResponse.onSuccess(count);
    }

    // 3) GET /rooms/{roomId}/remaining-seconds -> 만료 시각 기준 남은 초
    @GetMapping("/rooms/{roomId}/remaining-seconds")
    @Operation(summary = "채팅방 만료 시간 반환 api", description = "이야기 채팅방 만료까지 남은 시간을 반환합니다")
    public ApiResponse<Long> remainingSeconds(@PathVariable String roomId) {
        StoryRoom room = storyRoomRepository.findByRoomId(stripSrPrefix(roomId))
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));

        // StoryRoom에 expiredAt(만료 시각) 또는 유사 필드가 있다고 가정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = room.getExpiredAt(); // 필드명 다르면 맞게 변경

        if (expiredAt == null) {
            return ApiResponse.onSuccess(-1L); // 무제한(또는 프론트 합의 값)
        }
        long seconds = Duration.between(now, expiredAt).getSeconds();
        return ApiResponse.onSuccess(Math.max(0, seconds));
    }

    // 4) POST /rooms/{roomId}/enter -> StoryRoomParticipant 추가
    @PostMapping("/rooms/{roomId}/enter")
    @Operation(summary = "이야기 채팅방 입장 api", description = "이야기 채팅방에 참가하는 api입니다")
    public ApiResponse<String> enter(@PathVariable String roomId,
                                     @RequestParam Long memberId) {
        StoryRoom room = storyRoomRepository.findByRoomId(stripSrPrefix(roomId))
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        boolean alreadyJoined = participantRepository
                .existsByStoryRoomIdAndMemberId(room.getId(), member.getId());
        if (alreadyJoined) {
            return ApiResponse.onSuccess("이미 입장된 사용자입니다."); // or onFailure(에러코드)로 409 처리
        }

        StoryRoomParticipant p = new StoryRoomParticipant();
        p.setStoryRoom(room);
        p.setMember(member);
        p.setStoryRoomPost(room.getPost());
        p.setRole(StoryRoomParticipant.Role.PARTICIPANT);

        try {
            participantRepository.save(p);
            return ApiResponse.onSuccess("입장 완료");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 유니크 제약 충돌 → 이미 입장된 것으로 간주
            return ApiResponse.onSuccess("이미 입장된 사용자입니다.");
        }
    }

    // 5) POST /rooms/{roomId}/leave -> StoryRoomParticipant 삭제
    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "이야기방 채팅방 나가기 api", description = "이야기 채팅방에서 나가는 api입니다")
    public ApiResponse<String> leave(@PathVariable String roomId,
                                     @RequestParam Long memberId) {
        StoryRoom room = storyRoomRepository.findByRoomId(stripSrPrefix(roomId))
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다."));

        participantRepository.findAll().stream()
                .filter(p -> p.getStoryRoom().getId().equals(room.getId())
                        && p.getMember().getId().equals(memberId))
                .findFirst()
                .ifPresent(participantRepository::delete);

        return ApiResponse.onSuccess("퇴장 완료");
    }

    // 메시지: 읽기 / 보내기
    // 6) GET /rooms/{roomId}/messages?size=50&cursor={id}&direction=prev|next
    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "이야기방 채팅방 채팅 읽기 api", description = "이야기 채팅방 채팅 메시지 반환 api입니다")
    public ApiResponse<ChatMessagePageDTO> getMessages(@PathVariable String roomId,
                                                       @RequestParam(required = false) Long meId,
                                                       @RequestParam(required = false) Integer size,
                                                       @RequestParam(required = false) Long cursor,
                                                       @RequestParam(required = false, defaultValue = "prev") String direction) {
        ChatMessagePageDTO page = storyChatMessageService.readMessages(stripSrPrefix(roomId), meId, size, cursor, direction);
        return ApiResponse.onSuccess(page);
    }
}
