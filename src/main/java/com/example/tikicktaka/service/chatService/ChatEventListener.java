// src/main/java/com/example/tikicktaka/service/chatService/ChatEventListener.java
package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.tikicktaka.domain.images.ProfileImg;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.companionPostChat.ChatMessage;
import com.example.tikicktaka.domain.storyRoomChat.StoryChatMessage;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.ProfileImgRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.repository.storyRoomChat.StoryRoomChatMessageRepository;
import com.example.tikicktaka.web.dto.chat.ChatSocketPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final SocketIOServer server;

    // 저장만 담당 (emit 금지)
    private final ChatMessageService companionChatService;
    private final com.example.tikicktaka.service.storyChat.StoryChatMessageService storyChatService;

    // 판별/조회용
    private final CompanionPostChatRoomRepository companionRoomRepo;
    private final CompanionPostChatMessageRepository companionMsgRepo;
    private final StoryRoomRepository storyRoomRepo;
    private final StoryRoomChatMessageRepository storyMsgRepo;
    private final MemberRepository memberRepository;
    private final ProfileImgRepository profileImgRepository;

    enum RoomType { COMPANION, STORY }

    // 방 입장 (필수)
    @OnEvent("joinRoom")
    public void onJoinRoom(SocketIOClient client, Map<String, Object> data) {
        String roomId = String.valueOf(data.get("roomId")).trim();
        Object userId = data.get("userId");
        if (roomId.isBlank()) return;
        client.joinRoom(roomId);
        log.info("joinRoom: sid={}, userId={}, roomId={}, roomsNow={}",
                client.getSessionId(), userId, roomId, client.getAllRooms());
    }

    // (client, ack, data)
    @OnEvent("sendMessage")
    public void onSendMessage(SocketIOClient client, AckRequest ack, Map<String, Object> data) {
        // 0) 안전 파싱
        String roomId = String.valueOf(data.get("roomId")).trim();
        Object senderIdRaw = data.get("senderId");
        String message = String.valueOf(data.get("message"));

        if (roomId.isBlank() || senderIdRaw == null || message == null || message.isBlank()) {
            log.warn("Invalid sendMessage payload: {}", data);
            return;
        }
        Long senderId = (senderIdRaw instanceof Number)
                ? ((Number) senderIdRaw).longValue()
                : Long.valueOf(String.valueOf(senderIdRaw));

        log.info("sendMessage IN: roomId={}, senderId={}, msg='{}'", roomId, senderId, message);

        // 1) 방 타입 판별
        RoomType type = resolveRoomTypeSafe(roomId);

        // 2) 저장만 수행 (emit 금지)
        try {
            if (type == RoomType.STORY) {
                storyChatService.sendMessage(roomId, senderId, message);
            } else if (type == RoomType.COMPANION) {
                companionChatService.sendMessage(roomId, senderId, message);
            } else {
                // 폴백: 이야기→실패시 동행
                if (!tryStoryThenCompanion(roomId, senderId, message)) {
                    log.warn("No room matched for roomId={}, dropping message", roomId);
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Save failed: roomId={}, senderId={}", roomId, senderId, e);
            return;
        }
        // 3) 저장 직후 마지막 1건 재조회 → payload
        ChatSocketPayload payload = buildPayload(roomId, senderId);

        boolean ackRequested = ack != null && ack.isAckRequested();
        if (ackRequested) {
            // 본인: ACK로만 (에코 금지)
            ack.sendAckData(payload);
        } else {
            // 레거시/테스트: ACK 없으면 본인에게 에코 1회
            client.sendEvent("receiveMessage", payload);
        }

        // 4) 본인: ACK 있으면 ACK, 없으면 에코
        // 같은 방의 '다른' 클라이언트에게만 브로드캐스트
        BroadcastOperations roomOps = client.getNamespace().getRoomOperations(roomId);
        for (SocketIOClient c : roomOps.getClients()) {
            if (!c.getSessionId().equals(client.getSessionId())) {
                c.sendEvent("receiveMessage", payload);
            }
        }

        log.info("sendMessage OUT: roomId={}, msgId={}", roomId, payload.getId());
    }

    private RoomType resolveRoomTypeSafe(String roomId) {
        try {
            if (storyRoomRepo.findByRoomId(roomId).isPresent()) return RoomType.STORY;         // 이야기 우선
            if (companionRoomRepo.findByRoomId(roomId).isPresent()) return RoomType.COMPANION; // 동행
            if (roomId.startsWith("SR-")) return RoomType.STORY;
            if (roomId.startsWith("CR-")) return RoomType.COMPANION;
        } catch (Exception ignore) {}
        return null;
    }

    private boolean tryStoryThenCompanion(String roomId, Long senderId, String message) {
        try {
            storyChatService.sendMessage(roomId, senderId, message);
            return true;
        } catch (Exception storyFail) {
            try {
                companionChatService.sendMessage(roomId, senderId, message);
                return true;
            } catch (Exception compFail) {
                log.error("Both story/companion save failed. roomId={}, senderId={}", roomId, senderId, compFail);
                return false;
            }
        }
    }

    private ChatSocketPayload buildPayload(String roomId, Long senderId) {
        // 이야기방 우선 조회 → 없으면 동행 폴백
        Optional<StoryChatMessage> s = Optional.empty();
        try { s = storyMsgRepo.findTop1ByStoryRoom_RoomIdOrderByIdDesc(roomId); } catch (Exception ignore) {}
        if (s.isPresent()) return toStoryPayload(s.get(), roomId, senderId);

        ChatMessage c = companionMsgRepo.findTop1ByChatRoom_RoomIdOrderByIdDesc(roomId)
                .orElseThrow(() -> new IllegalStateException("Saved message not found in any repo. roomId=" + roomId));
        return toCompanionPayload(c, roomId, senderId);
    }

    private ChatSocketPayload toStoryPayload(StoryChatMessage last, String roomId, Long senderId) {
        String senderName = memberRepository.findById(senderId).map(m -> m.getName()).orElse(null);
        String senderProfileUrl = profileImgRepository.findByMember_Id(senderId).map(p -> p.getUrl()).orElse(null);
        return ChatSocketPayload.builder()
                .id(last.getId())
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .senderProfileUrl(senderProfileUrl) // null이어도 DTO가 항상 포함
                .message(last.getMessage())
                .createdAt(toIsoUtc(last.getTimestamp()))
                .build();
    }

    private ChatSocketPayload toCompanionPayload(ChatMessage last, String roomId, Long senderId) {
        String senderName = memberRepository.findById(senderId).map(m -> m.getName()).orElse(null);
        String senderProfileUrl = profileImgRepository.findByMember_Id(senderId).map(p -> p.getUrl()).orElse(null);
        return ChatSocketPayload.builder()
                .id(last.getId())
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .senderProfileUrl(senderProfileUrl)
                .message(last.getMessage())
                .createdAt(toIsoUtc(last.getTimestamp()))
                .build();
    }

    private String toIsoUtc(LocalDateTime ts) {
        Instant inst = (ts != null)
                ? ts.atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();
        return DateTimeFormatter.ISO_INSTANT.format(inst); // e.g. 2025-09-27T14:12:34Z
    }
}
