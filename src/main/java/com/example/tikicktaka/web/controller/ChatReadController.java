//package com.example.tikicktaka.web.controller;
//
//import com.example.tikicktaka.apiPayload.ApiResponse;
//import com.example.tikicktaka.service.chatService.ChatReadService;
//import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/chats")
//@RequiredArgsConstructor
//public class ChatReadController {
//
//    private final ChatReadService chatReadService;
//
//    @GetMapping("/companion/{roomId}/messages")
//    @Operation(summary = "동행 채팅방 메시지 읽기 (키셋 페이지네이션 + 차단 마스킹)")
//    public ApiResponse<ChatMessagePageDTO> readCompanion(
//            @PathVariable String roomId,
//            @RequestParam(required = false) Long cursor,
//            @RequestParam(defaultValue = "next") String dir,
//            @RequestParam(defaultValue = "20") int size,
//            Authentication authentication
//    ) {
//        try {
//            Long meId = Long.valueOf(authentication.getName()); // JWT에서 memberId로 name 사용 중
//            var result = chatReadService.readCompanionMessages(meId, roomId, dir, cursor, size);
//            return ApiResponse.onSuccess(result);
//        } catch (IllegalArgumentException e) {
//            return ApiResponse.onFailure("NOT_FOUND", e.getMessage(), null);
//        } catch (SecurityException e) {
//            return ApiResponse.onFailure("FORBIDDEN", e.getMessage(), null);
//        }
//    }
//}
