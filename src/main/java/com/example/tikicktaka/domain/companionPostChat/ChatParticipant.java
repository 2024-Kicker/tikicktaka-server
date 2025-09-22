package com.example.tikicktaka.domain.companionPostChat;

import com.example.tikicktaka.domain.member.Member;
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
    @JoinColumn(name = "chatRoom_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY) // Member와 연관관계 설정
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    public void markRead(Long lastMessageId) {
        this.lastReadMessageId = lastMessageId;
    }

    public static ChatParticipant create(ChatRoom chatRoom, Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        System.out.println("Creating ChatParticipant with chatRoom ID: " + chatRoom.getId() + ", member ID: " + member.getId());
        return ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
    }
}
