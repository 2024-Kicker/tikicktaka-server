package com.example.tikicktaka.converter.chat;

import com.example.tikicktaka.web.dto.chat.ChatGroupInviteResultDTO;
import com.example.tikicktaka.web.dto.chat.ChatRoomCreateResultDTO;
import com.example.tikicktaka.domain.enums.TargetType;

import java.time.LocalDateTime;


public class ChatConverter {



    public static ChatRoomCreateResultDTO toRoomCreateResultDTO(
            String roomId,
            Long targetId,
            TargetType targetType,
            Long ownerId,
            Long requesterId
    ) {
        return ChatRoomCreateResultDTO.builder()
                .roomId(roomId)
                .targetType(targetType)
                .targetId(targetId)
                .ownerId(ownerId)
                .requesterId(requesterId)
                .build();
    }

    public static ChatGroupInviteResultDTO toGroupInviteResultDTO(
            String roomId, String inviteCode, Long postId, Long ownerId
    ) {
        return ChatGroupInviteResultDTO.builder()
                .roomId(roomId)
                .inviteCode(inviteCode)
                .postId(postId)
                .ownerId(ownerId)
                //.expiresInSeconds(expiresInSeconds)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
