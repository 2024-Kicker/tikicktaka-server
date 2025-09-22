package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.converter.chat.ChatConverter;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.service.CompanionPostService.CompanionPostService;
import com.example.tikicktaka.service.chatService.ChatMessageService;
import com.example.tikicktaka.service.chatService.ChatReadService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.web.dto.chat.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@RequestMapping("/api/companion/chats")
@Tag(name = "CompanionChat", description = "동행찾기 채팅방 관련 API")
@RequiredArgsConstructor
public class CompanionChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final ChatReadService chatReadService;
    private final CompanionPostService companionPostService;

    // 1:1 채팅방 생성 API
    @PostMapping("/rooms/OneOnOne")
    @Operation(summary = "1:1 채팅방 생성 API", description = "사용자가 게시글 작성자에게 메시지를 보내기 위해 1:1 채팅방을 생성합니다.")
    public ApiResponse<ChatRoomCreateResultDTO> createOneOnOneChatRoom(@RequestParam Long postId, Authentication authentication) {
        // JWT 인증 확인 (로그인된 사용자 여부 체크)
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure("UNAUTHORIZED", "로그인이 필요합니다.", null);
        }

        Long memberId;
        try {
            memberId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return ApiResponse.onFailure("UNAUTHORIZED", "잘못된 인증 정보입니다.", null);
        }

        CompanionPost post = companionPostService.findById(postId);
        if (post == null) {
            return ApiResponse.onFailure("NOT_FOUND", "게시글을 찾을 수 없습니다.", null);
        }

        // 1:1 채팅방 생성
        Long ownerId = post.getAuthor().getId();
        String roomId = chatRoomService.createOneOnOneChatRoom(ownerId, memberId, postId);

        ChatRoomCreateResultDTO dto =
                ChatConverter.toRoomCreateResultDTO(roomId, postId, TargetType.COMPANION_POST, ownerId, memberId);

        return ApiResponse.onSuccess(dto);
    }


    // 단체 채팅방 생성 및 초대 코드 생성 API
    @PostMapping("/rooms/invite")
    @Operation(summary = "단체 채팅방 생성 및 초대 코드 생성 API", description = "일대일 채팅방에서 넘어갈 단체채팅방과 초대코드를 함께 생성합니다")
    public ApiResponse<ChatGroupInviteResultDTO> generateInviteCode(
            @RequestBody InviteRequestDTO request,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure("UNAUTHORIZED", "로그인이 필요합니다.", null);
        }

        final Long memberId;
        try {
            memberId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return ApiResponse.onFailure("UNAUTHORIZED", "잘못된 인증 정보입니다.", null);
        }

        //  요청으로 들어온 ownerId와 JWT 사용자 ID 비교
        if (!memberId.equals(request.getOwnerId())) {
            return ApiResponse.onFailure("FORBIDDEN", "권한이 없습니다.", null);
        }

        CompanionPost post = companionPostService.findById(request.getPostId());
        if (post == null) {
            return ApiResponse.onFailure("NOT_FOUND", "게시글을 찾을 수 없습니다.", null);
        }
        if (!post.getAuthor().getId().equals(memberId)) {
            return ApiResponse.onFailure("FORBIDDEN", "본인이 작성한 게시글만 단체 채팅방을 생성할 수 있습니다.", null);
        }

        if (chatRoomService.existsGroupRoomForPost(request.getPostId())) {
            return ApiResponse.onFailure("CONFLICT", "이미 해당 게시글에 대한 단체 채팅방이 존재합니다.", null);
        }

        // ChatRoomDTO를 생성하고, request에서 postId와 ownerId를 설정
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setPostId(request.getPostId());
        chatRoomDTO.setOwnerId(request.getOwnerId());

        // 채팅방 생성
        ChatRoomDTO chatRoom = chatRoomService.createChatRoom(chatRoomDTO);

        //Integer ttlSeconds = 10 * 60; // 10분
        //redisService.storeInviteCode(chatRoom.getInviteCode(), ttlSeconds); // 10분 설정

        // 생성된 채팅방의 초대 코드 반환
        ChatGroupInviteResultDTO dto = ChatConverter.toGroupInviteResultDTO(
                chatRoom.getRoomId(),          // roomId (ChatRoomDTO에 있다면)
                chatRoom.getInviteCode(),
                request.getPostId(),
                request.getOwnerId()
        );
        return ApiResponse.onSuccess(dto);
    }

    // 초대 코드로 채팅방 입장 API
    @PostMapping("/rooms/joinByInvite")
    @Operation(summary = "초대 코드로 동행 단체 채팅방 입장 API")
    public ApiResponse<String> joinRoomByInviteCode(@RequestParam String inviteCode, @RequestParam Long userId) {
        try {
            String roomId = chatRoomService.joinRoomByInviteCode(inviteCode, userId);
            return ApiResponse.onSuccess(roomId); // roomId 반환
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(), e.getMessage(), null);
        }
    }

    //(DB기반) 특정 채팅방의 메시지 불러오기
    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "동행 채팅방 메시지 읽기 (DB기반 ㅣ 키셋 페이지네이션 + 차단 마스킹)")
    public ApiResponse<ChatMessagePageDTO> readCompanion(
            @PathVariable String roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "next") String dir,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        try {
            Long meId = Long.valueOf(authentication.getName()); // JWT에서 memberId로 name 사용 중
            var result = chatReadService.readCompanionMessages(meId, roomId, dir, cursor, size);
            return ApiResponse.onSuccess(result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.onFailure("NOT_FOUND", e.getMessage(), null);
        } catch (SecurityException e) {
            return ApiResponse.onFailure("FORBIDDEN", e.getMessage(), null);
        }
    }


//    //(Redis) 특정 채팅방의 메시지 불러오기
//    @GetMapping("/rooms/{roomId}")
//    @Operation(summary = "동행 채팅방 메시지 불러오기(Redis기반)")
//    public ApiResponse<List<ChatMessageDTO>> getChatMessages(@PathVariable String roomId) {
//        List<ChatMessageDTO> messages = chatMessageService.getMessages(roomId);
//        return ApiResponse.onSuccess(messages);
//    }
}
