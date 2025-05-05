package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.member.Member;

import java.util.List;

public interface ChatParticipantService {
    void addParticipant(String roomId, Long userId); // 채팅방 참가자 추가

    //void removeParticipant(Long roomId, Long userId); // 채팅방 참가자 제거

    void removeParticipant(String roomId, Member member);

    List<ChatParticipant> getParticipants(String roomId);

    // ChatParticipantService 클래스
    int getParticipantCount(String roomId);

    boolean isUserInRoom(String roomId, Long userId);
}

