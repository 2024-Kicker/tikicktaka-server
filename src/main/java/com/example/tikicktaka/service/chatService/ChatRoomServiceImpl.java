package com.example.tikicktaka.service.chatService;
import com.example.tikicktaka.domain.chat.ChatRoom;
import com.example.tikicktaka.repository.chat.ChatRoomRepository;
import com.example.tikicktaka.service.chatService.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatRoom createOneToOneRoom(Long user1, Long user2) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .ownerId(user1)
                .participantId(user2)
                .isGroup(false)
                .build();
        return chatRoomRepository.save(room);
    }

    @Override
    public ChatRoom createGroupRoom(Long ownerId) {
        String roomId = UUID.randomUUID().toString();
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .ownerId(ownerId)
                .isGroup(true)
                .build();
        return chatRoomRepository.save(room);
    }

    @Override
    public Optional<ChatRoom> getChatRoom(String roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }
}