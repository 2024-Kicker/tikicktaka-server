package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.repository.member.ProfileImgRepository;
import com.example.tikicktaka.web.dto.chat.PostChatRoomItemDTO;
import com.example.tikicktaka.web.dto.chat.PostChatRoomListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.tikicktaka.domain.images.ProfileImg;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomsForPostServiceImpl implements ChatRoomsForPostService {

    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;
    private final ProfileImgRepository profileImgRepository;
    private final ChatReadService chatReadService;

    @Override
    @Transactional(readOnly = true)
    public PostChatRoomListResponseDTO myRoomsForPost(Long meId, Long postId) {
        List<ChatRoom> rooms = companionPostChatRoomRepository.findAllByCompanionPost_Id(postId);
        List<PostChatRoomItemDTO> items = new ArrayList<>();

        for (ChatRoom room : rooms) {
            if (!companionPostChatParticipantRepository.existsByChatRoomAndMemberId(room, meId)) {
                continue;
            }

            String roomId = room.getRoomId();
            boolean isGroup = Boolean.TRUE.equals(room.getIsGroup());

            List<ChatParticipant> participants =
                    companionPostChatParticipantRepository.findByChatRoom_CompanionPost_IdAndChatRoom_RoomId(postId, roomId);

            int participantCount = (int) (participants == null ? 0 :
                    participants.stream()
                            .map(p -> p.getMember() != null ? p.getMember().getId() : null)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count());

            List<Long> memberIds = (participants == null ? Collections.<ChatParticipant>emptyList() : participants)
                    .stream()
                    .filter(p -> p.getMember() != null && p.getMember().getId() != null)
                    .map(p -> p.getMember().getId())
                    .distinct()
                    .sorted((a, b) -> {
                        if (a.equals(meId)) return -1;
                        if (b.equals(meId)) return 1;
                        return 0;
                    })
                    .toList();

            String opponentName = null;
            if (!isGroup) {
                Long otherId = memberIds.stream()
                        .filter(id -> !id.equals(meId))
                        .findFirst()
                        .orElse(null);

                if (otherId != null) {
                    var otherMember = (participants == null ? Collections.<ChatParticipant>emptyList() : participants)
                            .stream()
                            .map(ChatParticipant::getMember)
                            .filter(Objects::nonNull)
                            .filter(m -> otherId.equals(m.getId()))
                            .findFirst()
                            .orElse(null);

                    opponentName = resolveDisplayName(otherMember);
                }
            }

            // 프로필 이미지 배치 조회 → 멤버ID→URL 매핑
            Map<Long, String> urlByMemberId = profileImgRepository.findByMember_IdIn(memberIds)
                    .stream()
                    .filter(pi -> pi.getMember() != null && pi.getMember().getId() != null)
                    .collect(Collectors.toMap(
                            pi -> pi.getMember().getId(),
                            pi -> pi.getUrl(),   // getUrl() 이름만 맞추기
                            (u1, u2) -> u1
                    ));

            // 기본 이미지 URL
            String fallback = "https://tikicktaka-bucket.s3.ap-northeast-2.amazonaws.com/logo/Companion_Baseball.png";
            List<String> profileImages = (participants == null ? Collections.<ChatParticipant>emptyList() : participants)
                    .stream()
                    .map(ChatParticipant::getMember)
                    .filter(Objects::nonNull)
                    .map(m -> {
                        var pi = m.getProfileImg();          // ← Member → ProfileImg 연관 직접 접근
                        String url = (pi != null) ? pi.getUrl() : null;
                        return (url != null && !url.isBlank()) ? url : fallback;
                    })
                    .collect(java.util.stream.Collectors.toList());

            // 마지막 메시지/시각
            var lastOpt = companionPostChatMessageRepository.findTop1ByChatRoom_RoomIdOrderByIdDesc(roomId);
            String lastMsg = lastOpt.map(ChatMessage::getMessage).orElse(null);
            LocalDateTime lastAt = lastOpt.map(ChatMessage::getTimestamp).orElse(null);

            int unread = 0;
            try {
                unread = chatReadService.countUnread(roomId, meId);
            } catch (Exception ignore) {
                unread = 0;
            }

            PostChatRoomItemDTO dto = PostChatRoomItemDTO.builder()
                    .roomId(roomId)
                    .group(isGroup)
                    .postId(postId)
                    .participants(participantCount)
                    .participantProfileImages(profileImages)
                    .opponentName(opponentName)
                    .lastMessage(lastMsg)
                    .lastAt(lastAt)
                    .unreadCount(unread)
                    .build();

            items.add(dto);
        }

        items.sort(Comparator.comparing(
                (PostChatRoomItemDTO i) -> i.getLastAt() == null ? LocalDateTime.MIN : i.getLastAt()
        ).reversed());

        return new PostChatRoomListResponseDTO(items);
    }

    private String resolveDisplayName(Member m) {
        if (m == null) return null;
        try {
            if (m.getName() != null && !m.getName().isBlank()) return m.getName();
        } catch (NoSuchMethodError | Exception ignored) {
        }
        try {
            if (m.getEmail() != null && !m.getEmail().isBlank()) {
                String email = m.getEmail();
                int at = email.indexOf('@');
                return (at > 0) ? email.substring(0, at) : email;
            }
        } catch (NoSuchMethodError | Exception ignored) {
        }
        return "알 수 없음";
    }
}