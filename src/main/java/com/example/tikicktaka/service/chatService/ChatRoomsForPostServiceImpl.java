package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
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

    private final CompanionPostChatRoomRepository roomRepo;
    private final CompanionPostChatParticipantRepository participantRepo;
    private final CompanionPostChatMessageRepository messageRepo;
    private final ProfileImgRepository profileImgRepository;

    @Override
    @Transactional(readOnly = true)
    public PostChatRoomListResponseDTO myRoomsForPost(Long meId, Long postId) {
        List<ChatRoom> rooms = roomRepo.findAllByCompanionPost_Id(postId);
        List<PostChatRoomItemDTO> items = new ArrayList<>();

        for (ChatRoom room : rooms) {
            if (!participantRepo.existsByChatRoomAndMemberId(room, meId)) {
                continue;
            }

            String roomId = room.getRoomId();
            boolean isGroup = Boolean.TRUE.equals(room.getIsGroup());

            List<ChatParticipant> participants =
                    participantRepo.findByChatRoom_CompanionPost_IdAndChatRoom_RoomId(postId, roomId);

            int participantCount = (participants != null) ? participants.size() : 0;

            //멤버 ID 추출 (원하면 meId 먼저 오도록 정렬)
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
            var lastOpt = messageRepo.findTop1ByChatRoom_RoomIdOrderByIdDesc(roomId);
            String lastMsg = lastOpt.map(ChatMessage::getMessage).orElse(null);
            LocalDateTime lastAt = lastOpt.map(ChatMessage::getTimestamp).orElse(null);

            // (임시) unread: 내 마지막 발신 이후 상대 메시지 수
            int unread = 0;
            try {
                Long myLastSentId = messageRepo
                        .findTop1ByChatRoom_RoomIdAndSenderIdOrderByIdDesc(roomId, meId)
                        .map(ChatMessage::getId)
                        .orElse(0L);

                unread = (int) messageRepo.countByChatRoomRoomIdAndIdGreaterThanAndSenderIdNot(
                        roomId, myLastSentId, meId
                );
            } catch (Exception ignore) {
                unread = 0;
            }

            PostChatRoomItemDTO dto = PostChatRoomItemDTO.builder()
                    .roomId(roomId)
                    .group(isGroup)
                    .postId(postId)
                    .participants(participantCount)
                    .participantProfileImages(profileImages)
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

}