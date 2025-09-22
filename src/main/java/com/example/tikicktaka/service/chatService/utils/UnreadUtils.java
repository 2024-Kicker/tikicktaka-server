package com.example.tikicktaka.service.chatService.utils;

import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;


public final class UnreadUtils {
    private UnreadUtils() {}

    public static Integer calcUnreadForPost(
            CompanionPostChatRoomRepository companionPostChatRoomRepository,
            CompanionPostChatParticipantRepository companionPostChatParticipantRepository,
            CompanionPostChatMessageRepository companionPostChatMessageRepository,
            Long postId, Long memberId) {

        return companionPostChatRoomRepository.findFirstByCompanionPost_IdAndIsGroupTrue(postId)
                .map(room -> {
                    String roomId = room.getRoomId();
                    Long lastRead = companionPostChatParticipantRepository
                            .findByChatRoom_RoomIdAndMember_Id(roomId, memberId)
                            .map(ChatParticipant::getLastReadMessageId)
                            .orElse(null);

                    if (lastRead == null) {
                        return (int) companionPostChatMessageRepository.countByChatRoom_RoomId(roomId);
                    }
                    return (int) companionPostChatMessageRepository.countByChatRoom_RoomIdAndIdGreaterThan(roomId, lastRead);
                })
                .orElse(0);
    }
}
