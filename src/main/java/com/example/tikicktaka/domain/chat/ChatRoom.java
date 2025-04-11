package com.example.tikicktaka.domain.chat;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 1:1 및 단체 채팅방을 관리
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId;  // 채팅방 ID

    @ManyToOne(fetch = FetchType.LAZY)  // Member와 연관관계
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;  // 채팅방 소유자 (Member)

    @ManyToOne(fetch = FetchType.LAZY)  // 1:1 채팅인 경우, 참가자도 Member로 연결
    @JoinColumn(name = "participant_id", nullable = true)
    private Member participant;  // 채팅방 참가자 (1:1 채팅인 경우)

    @Column(nullable = false)
    private Boolean isGroup;  // 그룹 채팅방 여부

    @Column(nullable = true)
    private String inviteCode;  // 초대 코드 추가

    @ManyToOne(fetch = FetchType.LAZY)  // CompanionPost와 연관관계
    @JoinColumn(name = "post_id", nullable = true)
    private CompanionPost companionPost;  // 게시글 ID (동행 게시물)

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;  // 채팅 메시지들

    @PrePersist
    public void generateRoomId() {
        if (this.roomId == null) {
            this.roomId = UUID.randomUUID().toString();  // UUID로 채팅방 ID 생성
        }
    }

    public void setRoomId(String s) {
    }

    public void setOwner(Member owner) {
        this.owner = owner;  // owner 필드에 Member 객체 설정
    }

    public void setCompanionPost(CompanionPost companionPost) {
        this.companionPost = companionPost;  // companionPost 필드에 CompanionPost 객체 설정
    }

}
