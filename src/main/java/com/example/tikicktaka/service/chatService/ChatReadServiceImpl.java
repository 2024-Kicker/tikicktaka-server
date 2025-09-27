package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatReadCursor;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.blocked.BlockedRepository;
import com.example.tikicktaka.repository.companionPostChat.ChatReadCursorRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.web.dto.chat.ChatMessageItemDTO;
import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatReadServiceImpl implements ChatReadService {

    private final ChatAuthFacadeService auth;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;
    private final MemberDisplayNameService memberDisplayNameService;
    private final BlockedRepository blockedRepository;
    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final ChatReadCursorRepository chatReadCursorRepository;
    private final MemberRepository memberRepository;


    @Override
    @Transactional
    public ChatMessagePageDTO readCompanionMessages(Long meId, String roomId,
                                                    String dir, Long cursor, int size) {
        // 권한
        boolean authorView = auth.resolveCompanion(meId, roomId).authorView();

        // 내가 차단한 사용자
        Set<Long> blocked = blockedRepository.findByMemberIdAndTargetTypeOrderByCreatedAtDesc(meId, TargetType.MEMBER)
                .stream()
                .map(b -> b.getTargetId())
                .collect(Collectors.toSet());

        var pageable = PageRequest.of(0, size);
        List<ChatMessage> raw;

        if (cursor == null) {
            // 첫 페이지: 최신부터 n개
            raw = companionPostChatMessageRepository.findByChatRoomRoomIdOrderByIdDesc(roomId, pageable);

        } else if ("prev".equalsIgnoreCase(dir)) {
            // prev = 더 최신으로 이동 (cursor 이후 = id > cursor, 오름차순으로 가져와서 나중에 asc 유지)
            raw = companionPostChatMessageRepository.findByChatRoomRoomIdAndIdGreaterThanOrderByIdAsc(roomId, cursor, pageable);

        } else { // dir == "next" (default)
            // next = 더 과거로 이동 (cursor 이전 = id < cursor, 내림차순으로 가져온 뒤 아래에서 asc로 정렬 통일)
            raw = companionPostChatMessageRepository.findByChatRoomRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, pageable);
        }

// 응답은 오름차로 통일
        raw.sort(Comparator.comparingLong(ChatMessage::getId));

        boolean isGroup = false;
        String inviteCode = null;
        Long tmpOwnerId = null;
        if (!raw.isEmpty()) {
            var room = raw.get(0).getChatRoom();         // 첫 메시지의 방
            if (room != null) {
                isGroup = Boolean.TRUE.equals(room.getIsGroup());   // boolean이면 room.isGroup()
                inviteCode = room.getInviteCode();
                if (room.getOwner() != null) {
                    tmpOwnerId = room.getOwner().getId();   //여기서 ownerId 세팅
                }
            }
        }
        final Long ownerId = tmpOwnerId;

        var names = memberDisplayNameService.namesOf(
                raw.stream().map(ChatMessage::getSenderId).distinct().toList()
        );

        var items = raw.stream().map(m -> {
            boolean isBlocked = blocked.contains(m.getSenderId());

            String senderRole = (ownerId != null && Objects.equals(m.getSenderId(), ownerId))
                    ? "OWNER" : "MEMBER";

            return ChatMessageItemDTO.builder()
                    .id(m.getId())
                    .senderId(m.getSenderId())
                    .senderName(isBlocked ? "차단됨" : names.getOrDefault(m.getSenderId(), null))
                    .content(isBlocked ? null : m.getMessage())
                    .createdAt(m.getTimestamp())
                    .fromBlockedUser(isBlocked)
                    .senderRole(senderRole)
                    .build();
        }).toList();

        String inviteCodeForResponse = null;
        if (!isGroup && authorView) {
            inviteCodeForResponse = inviteCode;
        }

        Long prev = items.isEmpty() ? null : items.get(0).getId();
        Long next = items.isEmpty() ? null : items.get(items.size() - 1).getId();

        if (!raw.isEmpty()) {
            Long lastSeenId = raw.get(raw.size() - 1).getId();
            companionPostChatParticipantRepository.findByChatRoom_RoomIdAndMember_Id(roomId, meId)
                    .ifPresent(cp -> cp.markRead(lastSeenId)); // JPA flush는 트랜잭션 종료 시 수행
        }

        return ChatMessagePageDTO.builder()
                .authorView(authorView)
                .prevCursor(prev)
                .nextCursor(next)
                .messages(items)
                .build();
    }

    @Override
    @Transactional
    public void markRead(String roomId, Long memberId, Long lastReadMessageId) {
        ChatReadCursor cursor = chatReadCursorRepository.findByRoomIdAndMember_Id(roomId, memberId)
                .orElseGet(() -> {
                    Member meRef = memberRepository.getReferenceById(memberId);
                    return new ChatReadCursor(roomId, meRef, null, null);
                });
        cursor.setLastReadMessageId(lastReadMessageId);
        cursor.setLastReadAt(LocalDateTime.now());
        chatReadCursorRepository.save(cursor);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUnread(String roomId, Long memberId) {
        Long lastReadId = chatReadCursorRepository.findByRoomIdAndMember_Id(roomId, memberId)
                .map(ChatReadCursor::getLastReadMessageId)
                .orElse(null);

        if (lastReadId == null || lastReadId == 0L) {
            // 아직 읽은 기록이 없으면, 내 메시지를 제외한 전체 카운트
            long c = companionPostChatMessageRepository.countByChatRoom_RoomIdAndSenderIdNot(roomId, memberId);
            return (int) c;
        }
        long c = companionPostChatMessageRepository.countByChatRoomRoomIdAndIdGreaterThanAndSenderIdNot(roomId, lastReadId, memberId);
        return (int) c;
    }
}

