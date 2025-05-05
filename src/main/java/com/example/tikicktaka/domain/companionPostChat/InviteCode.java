package com.example.tikicktaka.domain.companionPostChat;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String inviteCode;

    @OneToOne
    @JoinColumn(name = "chatRoom_id", nullable = false)
    private ChatRoom chatRoom;

    private LocalDateTime expiresAt;
}


