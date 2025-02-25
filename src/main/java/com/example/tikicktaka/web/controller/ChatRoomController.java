package com.example.tikicktaka.web.controller;

package com.example.tikicktaka.controller;

import com.example.tikicktaka.domain.chat.ChatRoom;
import com.example.tikicktaka.service.chat.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat-room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/one-to-one")
    public ResponseEntity<ChatRoom> createOneToOneRoom(@RequestParam Long user1, @RequestParam Long user2) {
        ChatRoom chatRoom = chatRoomService.createOneToOneRoom(user1, user2);
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatRoom> createGroupRoom(@RequestParam Long ownerId) {
        ChatRoom chatRoom = chatRoomService.createGroupRoom(ownerId);
        return ResponseEntity.ok(chatRoom);
    }
}

