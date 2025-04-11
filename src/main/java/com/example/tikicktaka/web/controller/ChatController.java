package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.service.chatService.ChatMessageService;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import com.example.tikicktaka.web.dto.chat.ChatMessageDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    //특정 채팅방의 메시지 불러오기
    @GetMapping("/{roomId}")
    @Operation(summary = "특정 채팅방의 메시지 불러오기")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable String roomId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    //특정 채팅방에 메시지 전송하기
    @PostMapping("/{roomId}")
    @Operation(summary = "특정 채팅방에 메시지 전송하기")
    public ResponseEntity<Void> sendMessage(@PathVariable String roomId, @RequestParam Long senderId, @RequestParam String message) {
        chatMessageService.saveMessage(roomId, senderId, message);
        return ResponseEntity.ok().build();
    }

    // 초대 코드로 채팅방 입장 API
    @PostMapping("/joinByInviteCode")
    @Operation(summary = "초대 코드로 채팅방 입장 API")
    public ResponseEntity<String> joinRoomByInviteCode(@RequestParam String inviteCode, @RequestParam Long userId) {
        try {
            //chatRoomService.joinRoomByInviteCode(inviteCode, userId);
            //return ResponseEntity.ok("채팅방에 성공적으로 입장했습니다.");
            String roomId = chatRoomService.joinRoomByInviteCode(inviteCode, userId);
            return ResponseEntity.ok(roomId); // roomId 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

