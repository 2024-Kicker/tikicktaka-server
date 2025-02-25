package com.example.tikicktaka.domain.chat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 채팅방과 연관관계 설정
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long userId;

    public static ChatParticipant create(ChatRoom chatRoom, Long userId) {
        return ChatParticipant.builder()
                .chatRoom(chatRoom)
                .userId(userId)
                .build();
    }
}
