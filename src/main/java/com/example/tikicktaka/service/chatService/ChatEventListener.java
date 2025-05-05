package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.chatService.ChatParticipantService;
import com.example.tikicktaka.web.dto.chat.JoinRoomRequest;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {
    private final SocketIOServer server;

    private final ChatParticipantService chatParticipantService;
//    private final ChatInviteService chatInviteService;
    private final ChatRoomService chatRoomService;
    private final MemberRepository memberRepository;

    @Autowired
    @Lazy
    private final ChatMessageService chatMessageService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    private static final Logger logger = LoggerFactory.getLogger(ChatEventListener.class);

    @PostConstruct
    public void init() {
        logger.info("ChatEventListener 빈이 생성되었습니다!");
        server.addListeners(this);  // 현재 클래스(ChatEventListener)를 리스너로 등록
    }

    //채팅방 입장
    @OnEvent("joinRoom")
    public void handleJoinRoom(SocketIOClient client, Object rawData) {
        logger.info("Raw joinRoom event data: {}", rawData);

        try {
            if (rawData == null) {
                logger.error("rawData is NULL! WebSocket 요청을 확인하세요.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            // 데이터를 Map<String, Object>로 변환
            Map<String, Object> data = objectMapper.convertValue(rawData, Map.class);
            logger.info("Received joinRoom event: {}", data);

            String roomId = (String) data.get("roomId");
            Integer userId = (Integer) data.get("userId");

            logger.info("Extracted data - roomId: {}, userId: {}", roomId, userId);
            if (roomId == null || userId == null) {
                logger.error("Missing required fields! roomId: {}, userId: {}", roomId, userId);
                return;
            }

            // 채팅방에 참가자가 이미 있는지 확인
            boolean isAlreadyParticipant = chatParticipantService.isUserInRoom(roomId, Long.valueOf(userId));
            if (isAlreadyParticipant) {
                logger.info("User {} is already a participant in room {}", userId, roomId);
            } else {
                // 데이터 검증 후 DB에 참가자 추가
                chatParticipantService.addParticipant(roomId, Long.valueOf(userId));
                logger.info("Added user {} as a participant to room {}", userId, roomId);
            }

            // 방에 참가
            client.joinRoom(roomId);
            logger.info("User {} joined room {}", userId, roomId);

        } catch (Exception e) {
            logger.error("Error parsing joinRoom event data", e);
        }
    }

    // 1:1 채팅방 생성 로직 추가
    @OnEvent("createOneOnOneChat")
    public void handleCreateOneOnOneChat(SocketIOClient client, Object rawData) {
        logger.info("Raw createOneOnOneChat event data: {}", rawData);

        try {
            if (rawData == null) {
                logger.error("rawData is NULL! WebSocket 요청을 확인하세요.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            // 데이터를 Map<String, Object>로 변환
            Map<String, Object> data = objectMapper.convertValue(rawData, Map.class);
            logger.info("Received createOneOnOneChat event: {}", data);

            Long userId = (Long) data.get("userId");
            Long targetUserId = (Long) data.get("targetUserId");
            Long postId = (Long) data.get("postId");  // postId 추가

            logger.info("Extracted data - userId: {}, targetUserId: {}", userId, targetUserId);

            if (userId == null || targetUserId == null) {
                logger.error("Missing required fields! userId: {}, targetUserId: {}", userId, targetUserId);
                return;
            }

            // 1:1 채팅방 생성
            String roomId = chatRoomService.createOneOnOneChatRoom(userId, targetUserId, postId);

            // 채팅방에 참가
            client.joinRoom(roomId);
            chatParticipantService.addParticipant(roomId, userId);
            chatParticipantService.addParticipant(roomId, targetUserId);

            logger.info("User {} created 1:1 companionPostChat with user {} in room {}", userId, targetUserId, roomId);

        } catch (Exception e) {
            logger.error("Error parsing createOneOnOneChat event data", e);
        }
    }

    // 1:1 채팅방에서 사용자가 나가면 채팅방 삭제
    @OnEvent("leaveRoom")
    public void handleLeaveOneOnOneChat(SocketIOClient client, Object rawData) {
        logger.info("Raw leaveOneOnOneChat event data: {}", rawData);

        try {
            if (rawData == null) {
                logger.error("rawData is NULL! WebSocket 요청을 확인하세요.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            // 데이터를 Map<String, Object>로 변환
            Map<String, Object> data = objectMapper.convertValue(rawData, Map.class);
            logger.info("Received leaveOneOnOneChat event: {}", data);

            String roomId = (String) data.get("roomId");
            Long userId = ((Number) data.get("userId")).longValue();

            logger.info("Extracted data - roomId: {}, userId: {}", roomId, userId);

            if (roomId == null || userId == null) {
                logger.error("Missing required fields! roomId: {}, userId: {}", roomId, userId);
                return;
            }

            // 채팅방에서 나가기
            client.leaveRoom(roomId);
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            chatParticipantService.removeParticipant(roomId, member);

            // 1:1 채팅방에서 한 명이 나가면 방을 삭제 (참가자가 0명이면 방 삭제)
            if (chatParticipantService.getParticipantCount(roomId) == 0) {
                chatRoomService.deleteChatRoom(roomId);
                logger.info("Chat room {} has been deleted as there are no participants left.", roomId);
            }

            logger.info("User {} left the 1:1 companionPostChat in room {}", userId, roomId);

        } catch (Exception e) {
            logger.error("Error parsing leaveOneOnOneChat event data", e);
        }
    }

    //메시지 전송
    @OnEvent("sendMessage")
    public void handleSendMessage(SocketIOClient client, Map<String, Object> data) {
        // data 예시: {"roomId": "250ac8a7-d073-411c-82b4-c2d2234571d9", "senderId": 2, "message": "Hello!"}
        String roomId = (String) data.get("roomId");
        Integer senderIdInt = (Integer) data.get("senderId");
        Long senderId = senderIdInt != null ? senderIdInt.longValue() : null;
        String message = (String) data.get("message");

        logger.info("Received sendMessage event: roomId={}, senderId={}, message={}", roomId, senderId, message);

        if (roomId == null || senderId == null || message == null) {
            logger.error("Invalid sendMessage data: {}", data);
            return;
        }

        try {
            // 메시지 저장 (Redis에 저장)
            chatMessageService.saveMessage(roomId, senderId, message); // Redis에 메시지 저장

            // 메시지 만료 시간 설정 (예: 14일 후 자동 삭제)
            redisTemplate.expire("companionPostChat:" + roomId, 14, TimeUnit.DAYS);  // 14일 후 메시지 삭제

            logger.info("Message saved successfully: roomId={}, senderId={}, message={}", roomId, senderId, message);

            // 해당 채팅방에 연결된 모든 클라이언트에게 메시지 전송 -> 이러면 자기 자신이 보낸 메시지도 자기한테 한번 더 옴
            client.getNamespace().getRoomOperations(roomId).sendEvent("receiveMessage", data);

            // 해당 채팅방에 연결된 모든 클라이언트에게 메시지 전송 (자기 자신 제외)
            logger.info("Clients in room {}: {}", roomId, server.getRoomOperations(roomId).getClients());
//            for (SocketIOClient roomClient : server.getRoomOperations(roomId).getClients()) {
//                // 현재 클라이언트와 다른 클라이언트에게 메시지 전송
//                if (!roomClient.getSessionId().equals(client.getSessionId())) {
//                    roomClient.sendEvent("receiveMessage", data);
//                    logger.info("Message sent to: {}", roomClient.getSessionId());
//                }
//            }
            logger.info("Message broadcasted to room {}", roomId);

        } catch (Exception e) {
            logger.error("Error processing sendMessage event", e);
        }
    }
}