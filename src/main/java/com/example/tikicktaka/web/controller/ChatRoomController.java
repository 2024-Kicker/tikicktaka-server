package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.service.CompanionPostService.CompanionPostService;
import com.example.tikicktaka.service.RedisService;
//import com.example.tikicktaka.service.chatService.ChatInviteService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.web.dto.chat.ChatRoomDTO;
import com.example.tikicktaka.web.dto.chat.InviteRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatRoom")
@RequiredArgsConstructor
public class ChatRoomController {

//    private final ChatInviteService chatInviteService;
    private final ChatRoomService chatRoomService; // 채팅방 생성 서비스
    private final RedisService redisService;  // RedisService 객체
    private final CompanionPostService companionPostService;


    // 단체 채팅방 생성 및 초대 코드 생성 API
    @PostMapping("/invite")
    @Operation(summary = "단체 채팅방 생성 및 초대 코드 생성 API", description = "본인이 작성한 게시글을 삭제합니다.")
    public ResponseEntity<String> generateInviteCode(@RequestBody InviteRequestDTO request, Authentication authentication) {
        // JWT 인증 확인 (로그인된 사용자 여부 체크)
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        //  JWT에서 사용자 ID 가져오기
        Long memberId;
        try {
            memberId = Long.valueOf(authentication.getName()); // JWT에서 가져온 사용자 ID
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 인증 정보입니다.");
        }

        //  요청으로 들어온 ownerId와 JWT 사용자 ID 비교
        if (!memberId.equals(request.getOwnerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
        }

        //  게시글 작성자 확인
        // request.getPostId()로 게시글을 찾아서 작성자가 로그인된 사용자와 일치하는지 확인
        CompanionPost post = companionPostService.findById(request.getPostId());  // 게시글 조회 로직 추가

        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글을 찾을 수 없습니다.");
        }

        // 게시글의 작성자를 확인 (Member 객체를 통해 비교)
        if (!post.getAuthor().getId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인이 작성한 게시글만 단체 채팅방을 생성할 수 있습니다.");
        }

        // 이미 해당 게시글에 채팅방이 존재하는지 확인
        if (chatRoomService.existsGroupRoomForPost(request.getPostId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 해당 게시글에 대한 단체 채팅방이 존재합니다.");
        }

        // ChatRoomDTO를 생성하고, request에서 postId와 ownerId를 설정
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setPostId(request.getPostId());
        chatRoomDTO.setOwnerId(request.getOwnerId());

        // 채팅방 생성
        ChatRoomDTO chatRoom = chatRoomService.createChatRoom(chatRoomDTO);

//        redisService.storeInviteCode(chatRoom.getInviteCode(), 10 * 60); // 10분 설정


        // 생성된 채팅방의 초대 코드 반환
        return ResponseEntity.ok(chatRoom.getInviteCode());
    }

    // 1:1 채팅방 생성 API
    @PostMapping("/createOneOnOne")
    @Operation(summary = "1:1 채팅방 생성 API", description = "사용자가 게시글 작성자에게 메시지를 보내기 위해 1:1 채팅방을 생성합니다.")
    public ResponseEntity<String> createOneOnOneChatRoom(@RequestParam Long postId, Authentication authentication) {
        // JWT 인증 확인 (로그인된 사용자 여부 체크)
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // JWT에서 사용자 ID 가져오기
        Long memberId;
        try {
            memberId = Long.valueOf(authentication.getName()); // JWT에서 가져온 사용자 ID
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 인증 정보입니다.");
        }

        // 게시글 조회
        CompanionPost post = companionPostService.findById(postId);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글을 찾을 수 없습니다.");
        }

        // 1:1 채팅방 생성
        String roomId = chatRoomService.createOneOnOneChatRoom(post.getAuthor().getId(), memberId, postId);

        return ResponseEntity.ok("1:1 채팅방이 성공적으로 생성되었습니다. Room ID: " + roomId);
    }



}


