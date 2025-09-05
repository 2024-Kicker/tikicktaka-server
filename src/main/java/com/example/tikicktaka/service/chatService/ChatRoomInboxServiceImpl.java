package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.web.dto.chat.ChatRoomListItemDTO;
import com.example.tikicktaka.web.dto.chat.ChatRoomListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;   // ✅ 스프링 Pageable
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomInboxServiceImpl implements ChatRoomInboxService {

    private final CompanionPostChatParticipantRepository participantRepo;
    private final CompanionPostChatMessageRepository messageRepo;

    @Override
    @Transactional(readOnly = true)
    public ChatRoomListResponseDTO getMyRooms(Long memberId, Long cursor, int size) {
        // 최신 활동순(= chatRoom.updatedAt DESC)으로 내가 속한 방 일부만 끊어오기
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "chatRoom.updatedAt"));
        List<ChatParticipant> parts = participantRepo.findByMember_Id(memberId, pageable);

        List<ChatRoomListItemDTO> items = new ArrayList<>(parts.size());

        for (ChatParticipant part : parts) {
            ChatRoom r = part.getChatRoom();
            if (r == null) continue;

            String roomId = r.getRoomId();

            // 마지막 메시지 1건
            Optional<ChatMessage> lastOpt = messageRepo.findTop1ByChatRoom_RoomIdOrderByIdDesc(roomId);
            if (lastOpt.isEmpty()) continue; // 메시지 없는 방은 스킵
            ChatMessage last = lastOpt.get();

            // 커서(=lastMessageId) 이전만 포함
            if (cursor != null && last.getId() >= cursor) continue;

            boolean isGroup = Boolean.TRUE.equals(r.getIsGroup());
            Long postId = (r.getCompanionPost() != null) ? r.getCompanionPost().getId() : null;

            // 참가자 수
            int participants = participantRepo.countByChatRoom_RoomId(roomId);

            // 방 제목 구성
            String roomTitle;
            if (isGroup) {
                roomTitle = "이 게시글 단체 채팅";
            } else {
                // 1:1 상대 한 명(나 제외)
                var peerPart = participantRepo.findFirstByChatRoom_RoomIdAndMember_IdNot(roomId, memberId);
                String peerName = peerPart.map(ChatParticipant::getMember)
                        .map(this::displayNameOf)      // Member에서 직접 이름 뽑기
                        .orElse("1:1 채팅");
                roomTitle = (peerName == null || peerName.isBlank()) ? "1:1 채팅" : peerName;
            }

            items.add(new ChatRoomListItemDTO(
                    roomId,
                    "COMPANION",
                    isGroup,
                    postId,
                    roomTitle,
                    participants,
                    last.getMessage(),
                    last.getTimestamp(),
                    last.getId()
            ));
        }

        // 안전하게 마지막 메시지 id 기준 재정렬
        items.sort(Comparator.comparing(ChatRoomListItemDTO::getLastMessageId).reversed());

        Long nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getLastMessageId();
        return new ChatRoomListResponseDTO(nextCursor, items);
    }

    private String displayNameOf(Member m) {
        if (m == null) return null;
        if (hasText(getOrNull(() -> m.getName()))) return m.getName();
        if (hasText(getOrNull(() -> m.getEmail()))) return m.getEmail();
        return null;
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String getOrNull(SupplierWithException<String> s) {
        try { return s.get(); } catch (Exception e) { return null; }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get();
    }
}
