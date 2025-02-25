package com.example.tikicktaka.domain.chat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public static ChatMessage create(ChatRoom chatRoom, Long senderId, String message) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
