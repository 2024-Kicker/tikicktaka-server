package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.service.chatService.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/{roomId}")
    public ResponseEntity<List<String>> getChatMessages(@PathVariable String roomId) {
        List<String> messages = chatMessageService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<Void> sendMessage(@PathVariable String roomId, @RequestParam Long senderId, @RequestParam String message) {
        chatMessageService.saveMessage(roomId, senderId, message);
        return ResponseEntity.ok().build();
    }
}

