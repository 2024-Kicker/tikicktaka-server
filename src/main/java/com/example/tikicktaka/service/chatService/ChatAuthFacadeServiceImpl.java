package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatAuthFacadeServiceImpl implements ChatAuthFacadeService {

    private final CompanionPostChatRoomRepository roomRepo;
    private final CompanionPostChatParticipantRepository partRepo;

    @Override
    public AuthResult resolveCompanion(Long meId, String roomId) {
        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        boolean isOwner = room.getOwner().getId().equals(meId);
        boolean isParticipant = partRepo.existsByChatRoomAndMemberId(room, meId);

        if (!(isOwner || isParticipant)) {
            throw new SecurityException("채팅방 참여자가 아닙니다.");
        }
        return new AuthResult(isOwner);
    }
}
