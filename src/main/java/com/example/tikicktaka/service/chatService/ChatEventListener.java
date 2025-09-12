package com.example.tikicktaka.service.chatService;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomParticipantRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomPostRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.repository.storyRoomChat.StoryRoomChatMessageRepository;
import com.example.tikicktaka.service.CompanionPostService.CompanionChatAuthService;
import com.example.tikicktaka.service.chatService.ChatParticipantService;
import com.example.tikicktaka.service.storyChat.StoryChatAuthService;
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
    private final RedisTemplate<String, Object> redisTemplate; // final로


    private final ChatParticipantService chatParticipantService;
    private final ChatRoomService chatRoomService;
    private final MemberRepository memberRepository;

    @Lazy
    private final ChatMessageService chatMessageService;

    // ===== 신규 의존성 (공용 라우팅) =====
    private final RoomResolver roomResolver;                      // SR-/CR- 모두 판별
    private final StoryChatAuthService storyChatAuthService;      // 이야기방 권한/만료/차단 체크 훅
    private final CompanionChatAuthService companionChatAuthService; // 동행방 권한 체크 훅
    private final ChatGateway chatGateway;                        // 메시지 읽기/쓰기 공용 라우팅

    private final StoryRoomRepository storyRoomRepository;
    private final StoryRoomParticipantRepository storyRoomParticipantRepository;

    private static final long CHAT_TTL_DAYS = 14;

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
            Map<String, Object> data = objectMapper.convertValue(rawData, Map.class);
            logger.info("Received joinRoom event: {}", data);

            String roomId = (String) data.get("roomId");
            Number userIdNum = (Number) data.get("userId");
            Long userId = (userIdNum == null) ? null : userIdNum.longValue();

            logger.info("Extracted data - roomId: {}, userId: {}", roomId, userId);
            if (roomId == null || userId == null) {
                logger.error("Missing required fields! roomId: {}, userId: {}", roomId, userId);
                return;
            }

            // 공용 룸 판별
            var resolved = roomResolver.resolve(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 roomId: " + roomId));

            // 권한/입장 가능 여부 체크 (도메인별)
            switch (resolved.type()) {
                case STORY -> {
                    storyChatAuthService.ensureEnterable(resolved.roomPk(), userId);
                    // 참가자 등록(간단 버전): story_room_participant에 없으면 추가
                    StoryRoom room = storyRoomRepository.findById(resolved.roomPk())
                            .orElseThrow(() -> new IllegalArgumentException("이야기방 없음(pk=" + resolved.roomPk() + ")"));
                    boolean exists = storyRoomParticipantRepository.existsByStoryRoomIdAndMemberId(room.getId(), userId);
                    if (!exists) {
                        Member member = memberRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
                        StoryRoomParticipant p = new StoryRoomParticipant();
                        p.setStoryRoom(room);
                        p.setMember(member);
                        // 필요한 경우 역할/추가 필드 세팅
                        storyRoomParticipantRepository.save(p);
                        logger.info("Added STORY participant {} to room {}", userId, resolved.roomId());
                    } else {
                        logger.info("STORY participant {} already exists in room {}", userId, resolved.roomId());
                    }
                }
                case COMPANION -> {
                    companionChatAuthService.ensureEnterable(resolved.roomPk(), userId);
                    // 기존 동행 참가자 서비스 그대로 사용
                    boolean isAlreadyParticipant = chatParticipantService.isUserInRoom(resolved.roomId(), userId);
                    if (!isAlreadyParticipant) {
                        chatParticipantService.addParticipant(resolved.roomId(), userId);
                        logger.info("Added COMPANION participant {} to room {}", userId, resolved.roomId());
                    } else {
                        logger.info("COMPANION participant {} already exists in room {}", userId, resolved.roomId());
                    }
                }
            }

            // 소켓 룸 입장 (공통 roomId 사용)
            client.joinRoom(resolved.roomId());
            logger.info("User {} joined room {} (type={})", userId, resolved.roomId(), resolved.type());

        } catch (Exception e) {
            logger.error("Error parsing joinRoom event data", e);
        }
    }

    // 1:1 채팅방 생성 로직 추가 (동행찾기 전용)
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

    // 공용 퇴장: STORY/COMPANION 모두 처리
    @OnEvent("leaveRoom")
    public void handleLeaveRoom(SocketIOClient client, Object rawData) {
        logger.info("Raw leaveRoom event data: {}", rawData);

        try {
            if (rawData == null) {
                logger.error("rawData is NULL! WebSocket 요청을 확인하세요.");
                return;
            }

            Map<String, Object> data = new ObjectMapper().convertValue(rawData, Map.class);
            String roomId = (String) data.get("roomId");
            Long userId = data.get("userId") == null ? null : ((Number) data.get("userId")).longValue();

            if (roomId == null || userId == null) {
                logger.error("Missing required fields! roomId: {}, userId: {}", roomId, userId);
                return;
            }

            client.leaveRoom(roomId); // 소켓 룸 탈퇴

            var resolved = roomResolver.resolve(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 roomId: " + roomId));

            switch (resolved.type()) {
                case STORY -> {
                    var room = storyRoomRepository.findById(resolved.roomPk())
                            .orElseThrow(() -> new IllegalArgumentException("이야기방 없음"));
                    // 참가자 삭제
                    storyRoomParticipantRepository.deleteByStoryRoomIdAndMemberId(room.getId(), userId);

                    // 남은 인원 수 계산
                    int left = storyRoomParticipantRepository.countByStoryRoomId(room.getId());

                    // 정책: 1명 이하 남으면 방 삭제
                    if (left <= 1) {
                        storyRoomRepository.delete(room);
                        logger.info("STORY room {} deleted ({} participants left).", roomId, left);
                    } else {
                        logger.info("User {} left STORY room {} ({} participants left).", userId, roomId, left);
                    }
                }
                case COMPANION -> {
                    Member member = memberRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

                    chatParticipantService.removeParticipant(roomId, member);

                    int left = chatParticipantService.getParticipantCount(roomId);

                    // 1:1 방이면 한 명 이하 남을 때 삭제, 그룹방 정책도 같게 쓸거면 그대로 둠
                    if (left <= 1) {
                        chatRoomService.deleteChatRoom(roomId);
                        logger.info("COMPANION room {} deleted ({} participants left).", roomId, left);
                    } else {
                        logger.info("User {} left COMPANION room {} ({} participants left).", userId, roomId, left);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error parsing leaveRoom event data", e);
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
            //chatMessageService.saveMessage(roomId, senderId, message); // Redis에 메시지 저장
            chatGateway.send(roomId, senderId, message);

            /// TTL 설정 (룸 타입에 따라 키 분기)
            var resolved = roomResolver.resolve(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 roomId: " + roomId));

            String redisKey = switch (resolved.type()) {
                case STORY     -> "storyChat:" + roomId;            // 이야기방 키
                case COMPANION -> "companionPostChat:" + roomId;    // 동행방 키(기존 유지)
            };
            redisTemplate.expire(redisKey, CHAT_TTL_DAYS, TimeUnit.DAYS);
            logger.info("Set Redis TTL {} days on key {}", CHAT_TTL_DAYS, redisKey);


            // 해당 채팅방에 연결된 모든 클라이언트에게 메시지 전송 -> 이러면 자기 자신이 보낸 메시지도 자기한테 한번 더 옴
            //client.getNamespace().getRoomOperations(roomId).sendEvent("receiveMessage", data);

            // 해당 채팅방에 연결된 모든 클라이언트에게 메시지 전송 (자기 자신 제외)
            logger.info("Clients in room {}: {}", roomId, server.getRoomOperations(roomId).getClients());
            for (SocketIOClient roomClient : server.getRoomOperations(roomId).getClients()) {
                if (!roomClient.getSessionId().equals(client.getSessionId())) {
                    roomClient.sendEvent("receiveMessage", data);
                }
            }
            logger.info("Message broadcasted to room {}", roomId);

        } catch (Exception e) {
            logger.error("Error processing sendMessage event", e);
        }
    }
}