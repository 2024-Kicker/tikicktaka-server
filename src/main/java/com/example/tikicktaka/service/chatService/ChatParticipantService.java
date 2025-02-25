package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.chat.ChatParticipant;

import java.util.List;

public interface ChatParticipantService {
    void addParticipant(Long roomId, Long userId); // 채팅방 참가자 추가
    void removeParticipant(Long roomId, Long userId); // 채팅방 참가자 제거
    List<ChatParticipant> getParticipants(Long roomId); // 채팅방 참가자 조회
}

