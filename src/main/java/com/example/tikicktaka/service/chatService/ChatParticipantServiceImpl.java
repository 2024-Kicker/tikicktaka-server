package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.chat.ChatParticipant;
import com.example.tikicktaka.repository.chat.ChatParticipantRepository;
import com.example.tikicktaka.service.chatService.ChatParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public void addParticipant(Long roomId, Long userId) {
        ChatParticipant participant = new ChatParticipant(roomId, userId);
        chatParticipantRepository.save(participant);
    }

    @Override
    public void removeParticipant(Long roomId, Long userId) {
        chatParticipantRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public List<ChatParticipant> getParticipants(Long roomId) {
        return chatParticipantRepository.findByRoomId(roomId);
    }
}
