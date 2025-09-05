package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatRoomSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomSummaryServiceImpl implements ChatRoomSummaryService {

    private final ChatAuthFacadeService auth;
    private final CompanionPostChatRoomRepository roomRepo;
    private final CompanionPostChatParticipantRepository participantRepo;
    private final CompanionPostChatMessageRepository messageRepo;

    @Override
    @Transactional(readOnly = true)
    public ChatRoomSummaryDTO getSummary(Long meId, String roomId) {
        var authView = auth.resolveCompanion(meId, roomId);
        boolean authorView = authView.authorView();

        ChatRoom room = roomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        boolean isGroup = Boolean.TRUE.equals(room.getIsGroup());
        Long postId = (room.getCompanionPost() != null) ? room.getCompanionPost().getId() : null;

        // 마지막 메시지
        Optional<ChatMessage> lastOpt = messageRepo.findTop1ByChatRoom_RoomIdOrderByIdDesc(roomId);
        String lastMsg = lastOpt.map(ChatMessage::getMessage).orElse(null);
        var lastAt  = lastOpt.map(ChatMessage::getTimestamp).orElse(null);

        // 참가자 수
        int participants = participantRepo.countByChatRoom_RoomId(roomId);

        // 게시글 헤더
        String postTitle = (room.getCompanionPost() != null) ? room.getCompanionPost().getTitle() : null;
        var postCreatedAt = (room.getCompanionPost() != null) ? room.getCompanionPost().getCreatedAt() : null;
        String postContent = (room.getCompanionPost() != null) ? room.getCompanionPost().getContent() : null; // 필요에 맞게 요약해도 OK
        String postShareLink = (postId != null) ? ("/posts/" + postId) : null; // 프로젝트 공유 링크 규칙에 맞게 변경

        // 초대코드: 작성자라면 단체방 코드 노출(1:1 화면이어도 "해당 게시글의 단체방" 코드 반환)
        String inviteCode = null;
        if (authorView && postId != null) {
            inviteCode = roomRepo.findFirstByCompanionPost_IdAndIsGroupTrue(postId)
                    .map(ChatRoom::getInviteCode)
                    .orElse(null);
        }

        String type = isGroup ? "GROUP" : "DIRECT";

        return new ChatRoomSummaryDTO(
                roomId,
                type,
                isGroup,
                postId,
                postTitle,
                postCreatedAt,
                postContent,
                postShareLink,
                authorView,
                inviteCode,
                participants,
                lastMsg,
                lastAt
        );
    }
}

