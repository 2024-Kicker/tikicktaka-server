package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatLinkedPostDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatLinkedPostServiceImpl implements ChatLinkedPostService {

    private final CompanionPostChatParticipantRepository participantRepository;
    private final CompanionPostChatRoomRepository roomRepository;
    private final CompanionPostChatMessageRepository messageRepository;
    private final CompanionPostRepository companionPostRepository;

    @Value("${companion.default-images.baseball}")
    private String defaultBaseballImage;

    @Value("${companion.default-images.travel}")
    private String defaultTravelImage;

    @Override
    public List<ChatLinkedPostDTO> listMyLinkedCompanionPosts(Long memberId) {
        // 1) 단체방: chat_participant에서 내가 속한 방
        List<ChatParticipant> myParticipations =
                participantRepository.findByMember_Id(memberId, Pageable.unpaged());

        // 2) 1:1 방: chat_room.owner == me OR chat_room.participant == me
        List<ChatRoom> directRooms =
                roomRepository.findByOwner_IdOrParticipant_Id(memberId, memberId);

        // 3) 방 합치고 Set으로 중복 제거
        Set<ChatRoom> myRooms = new HashSet<>();
        for (ChatParticipant p : myParticipations) {
            if (p.getChatRoom() != null) myRooms.add(p.getChatRoom());
        }
        myRooms.addAll(directRooms);

        if (myRooms.isEmpty()) return Collections.emptyList();

        // 4) 방 → 게시글 매핑 + 게시글별 최근 대화 시각 계산
        Map<Long, LocalDateTime> postLatestTalkAt = new HashMap<>();

        for (ChatRoom room : myRooms) {
            CompanionPost post = room.getCompanionPost();
            if (post == null || post.getId() == null) continue;

            LocalDateTime lastTalkAt = messageRepository
                    .findTop1ByChatRoom_RoomIdOrderByIdDesc(room.getRoomId())
                    .map(ChatMessage::getTimestamp)
                    .orElse(room.getUpdatedAt());

            postLatestTalkAt.merge(
                    post.getId(),
                    lastTalkAt != null ? lastTalkAt : LocalDateTime.MIN,
                    (oldV, newV) -> newV.isAfter(oldV) ? newV : oldV
            );
        }

        if (postLatestTalkAt.isEmpty()) return Collections.emptyList();

        // 5) 게시글 일괄 조회
        List<Long> postIds = new ArrayList<>(postLatestTalkAt.keySet());
        List<CompanionPost> posts = companionPostRepository.findAllByIdIn(postIds);

        // 6) 썸네일 보정 (없을 경우 도메인 규칙 기본 이미지)
        for (CompanionPost post : posts) {
            ensureThumbnailOrFallback(post);
        }

        // 7) DTO 변환 + 최근 대화 시각 기준 정렬
        return posts.stream()
                .sorted((a, b) -> {
                    LocalDateTime atA = postLatestTalkAt.getOrDefault(a.getId(), LocalDateTime.MIN);
                    LocalDateTime atB = postLatestTalkAt.getOrDefault(b.getId(), LocalDateTime.MIN);
                    return atB.compareTo(atA);
                })
                .map(p -> new ChatLinkedPostDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getContent(),
                        p.getThumbnailUrl(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private void ensureThumbnailOrFallback(CompanionPost post) {
        if (post == null) return;
        if (!StringUtils.hasText(post.getThumbnailUrl())) {
            String fallback = (post.getPostType() == CompanionPost.PostType.Travel)
                    ? defaultTravelImage
                    : defaultBaseballImage;
            post.setThumbnailUrl(fallback);
        }
    }
}
