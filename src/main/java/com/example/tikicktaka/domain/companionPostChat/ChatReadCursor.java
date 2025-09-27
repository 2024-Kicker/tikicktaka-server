package com.example.tikicktaka.domain.companionPostChat;

import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_read_cursor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "member_id"}))
public class ChatReadCursor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId; // ChatRoom.roomId (문자 키)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId; // 마지막으로 읽은 메시지의 PK (null이면 아직 안읽음)

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt; // 참고용 타임스탬프

    protected ChatReadCursor() {}

    public ChatReadCursor(String roomId, Member member, Long lastReadMessageId, LocalDateTime lastReadAt) {
        this.roomId = roomId;
        this.member = member;
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = lastReadAt;
    }

    // getters/setters
    public Long getId() { return id; }
    public String getRoomId() { return roomId; }
    public Member getMember() { return member; }
    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long v) { this.lastReadMessageId = v; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime t) { this.lastReadAt = t; }
}
