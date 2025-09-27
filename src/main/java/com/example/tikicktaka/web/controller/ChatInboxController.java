package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.service.chatService.*;
import com.example.tikicktaka.web.dto.chat.ChatRoomListResponseDTO;
import com.example.tikicktaka.web.dto.chat.ChatRoomSummaryDTO;
import com.example.tikicktaka.web.dto.chat.PostChatRoomListResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.tikicktaka.web.dto.chat.ChatLinkedPostDTO;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "ChatRoom LIST", description = "채팅방 목록 리스트 api")
public class ChatInboxController {

    private final ChatRoomInboxService inboxService;
    private final ChatRoomSummaryService summaryService;
    private final ChatParticipantService chatParticipantService;
    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final ChatBlockService chatBlockService;
    private final ChatLinkedPostService chatLinkedPostService;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;
    private final ChatRoomsForPostService chatRoomsForPostService;





    @GetMapping("/rooms")
    @Operation(summary = "내가 속한 전체 채팅방(동행찾기) 목록 - 마지막 메시지 포함(커서 기반)")
    public ApiResponse<ChatRoomListResponseDTO> myRooms(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Long meId = Long.valueOf(authentication.getName()); // 기존 컨벤션에 맞춤
        return ApiResponse.onSuccess(inboxService.getMyRooms(meId, cursor, size));
    }

    @GetMapping("/rooms/{roomId}/summary")
    @Operation(summary = "특정 채팅방 게시글 요약 조회(게시글 헤더 + 초대코드)")
    public ApiResponse<ChatRoomSummaryDTO> summary(
            @PathVariable String roomId,
            Authentication authentication
    ) {
        Long meId = Long.valueOf(authentication.getName());
        return ApiResponse.onSuccess(summaryService.getSummary(meId, roomId));
    }

    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "채팅방 나가기 api")
    public ApiResponse<Void> leave(
            @PathVariable String roomId,
            org.springframework.security.core.Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        chatParticipantService.leave(roomId, userId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/rooms/{roomId}/participants/count")
    @Operation(summary = "단체방 인원수 반환 api")
    public ApiResponse<Integer> participants(
            @PathVariable String roomId
    ) {
        int cnt = companionPostChatParticipantRepository.countByChatRoom_RoomId(roomId);
        return ApiResponse.onSuccess(cnt);
    }
// 이야기방 반환시간


    @PostMapping("/block")
    @Operation(summary = "특정 사용자를 차단하는 API.")
    public ApiResponse<Void> block(
            @RequestParam Long targetMemberId,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        chatBlockService.block(userId, targetMemberId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/api/chats/block")
    @Operation(summary = "기존에 차단한 사용자를 차단 해제하는 API.")
    public ApiResponse<Void> unblock(
            @RequestParam Long targetMemberId,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        chatBlockService.unblock(userId, targetMemberId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/block/list")
    @Operation(summary = "내가 차단한 사용자 목록을 조회하는 API.")
    public ApiResponse<List<Long>> blocks(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        return ApiResponse.onSuccess(chatBlockService.list(userId));
    }

    @GetMapping("/companion/active-posts")
    @Operation(
            summary = "내가 참여 중인 동행찾기 채팅방과 연계된 게시글 목록 반환",
            description = "채팅방 소속(1:1 + 단체)을 기준으로 연결된 동행찾기 게시글을 모아서 반환합니다. "
                    + "중복 게시글은 제거되며, 최근 대화 시각 기준으로 정렬됩니다."
    )
    public ApiResponse<List<ChatLinkedPostDTO>> myLinkedPosts(Authentication authentication) {
        Long me = Long.valueOf(authentication.getName());
        List<ChatLinkedPostDTO> result = chatLinkedPostService.listMyLinkedCompanionPosts(me);
        return ApiResponse.onSuccess(result);
    }


    @GetMapping("/companion/{postId}/active-rooms")
    @Operation(summary = "해당 동행글에서 생성된 채팅방 중 내가 속한 방 목록 반환",
            description = "단체/1:1 구분, 참여자 프로필 이미지, (임시)안읽은 메시지 수, 마지막 메시지/시각 포함.\n정렬: 마지막 메시지 시각 내림차순")
    public ApiResponse<PostChatRoomListResponseDTO> myRoomsForPost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long meId = Long.valueOf(authentication.getName());
        var result = chatRoomsForPostService.myRoomsForPost(meId, postId);
        return ApiResponse.onSuccess(result);
    }


    @GetMapping("/rooms/{roomId}/unread")
    @Operation(summary = "특정 채팅방의 (임시) 안읽은 메시지 수 반환")
    public ApiResponse<Integer> unread(
            @PathVariable String roomId,
            Authentication authentication
    ) {
        Long meId = Long.valueOf(authentication.getName());
        int unread = computeUnread(roomId, meId);
        return ApiResponse.onSuccess(unread);
    }


    private int computeUnread(String roomId, Long meId) {
        // 내가 마지막으로 보낸 메시지 ID (없으면 0L)
        Long myLastSentId = companionPostChatMessageRepository
                .findTop1ByChatRoom_RoomIdAndSenderIdOrderByIdDesc(roomId, meId)
                .map(ChatMessage::getId)
                .orElse(0L);

        // 그 이후 들어온 '상대' 메시지 수
        long cnt = companionPostChatMessageRepository
                .countByChatRoomRoomIdAndIdGreaterThanAndSenderIdNot(roomId, myLastSentId, meId);

        return (int) cnt;
    }


}
