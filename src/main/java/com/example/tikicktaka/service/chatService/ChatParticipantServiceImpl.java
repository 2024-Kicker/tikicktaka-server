package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.config.SocketIOConfig;
import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final MemberRepository memberRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatParticipantServiceImpl.class);
//    private final Logger logger = LoggerFactory.getLogger(SocketIOConfig.class);


    private static final String CR_PREFIX = "CR-";
    private String stripCrPrefix(String id) {
        return (id != null && id.startsWith(CR_PREFIX)) ? id.substring(CR_PREFIX.length()) : id;
    }

    @Override
    public void addParticipant(String roomId, Long userId) {
        logger.info("addParticipant called with roomId={}, userId={}", roomId, userId);
        String raw = stripCrPrefix(roomId);

        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("Found chatRoom: {}", chatRoom.getRoomId());

        // userId로 회원(Member) 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        logger.info("Found member: {}", member.getId());

        // ChatParticipant 객체 생성
        ChatParticipant participant = ChatParticipant.create(chatRoom, member);
        companionPostChatParticipantRepository.save(participant);

        logger.info("Successfully added participant: userId={} to roomId={}", userId, roomId);
    }

    // 채팅방에서 참가자 제거
    @Override
    @Transactional
    public void removeParticipant(String roomId, Member member) {
        String raw = stripCrPrefix(roomId); // 접두사 제거

        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("Removing participant: memberId={} from roomId={}", member.getId(), roomId);

        // 현재 멤버 제거
        companionPostChatParticipantRepository.deleteByChatRoomAndMember(chatRoom, member);

        // 남은 참가자 수 확인
        int remainingCount = companionPostChatParticipantRepository.countByChatRoom(chatRoom);
        logger.info("Remaining participants in room {}: {}", roomId, remainingCount);

//        // 참가자가 한 명 이하라면 채팅방 삭제
//        if (remainingCount <= 1) {
//            // 남은 참가자 전체 제거
//            List<ChatParticipant> remainingParticipants = companionPostChatParticipantRepository.findByChatRoom(chatRoom);
//            companionPostChatParticipantRepository.deleteAll(remainingParticipants);
//
//            // 채팅방 삭제
//            companionPostChatRoomRepository.delete(chatRoom);
//            logger.info("Deleted companionPostChat room {} because it only had one or no participants", roomId);
//        }
    }

    @Override
    public List<ChatParticipant> getParticipants(String roomId) {
        String raw = stripCrPrefix(roomId);

        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        return companionPostChatParticipantRepository.findByChatRoom(chatRoom);
    }

    // 채팅방 참가자 수 확인
    @Override
    public int getParticipantCount(String roomId) {
        String raw = stripCrPrefix(roomId);
        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        return companionPostChatParticipantRepository.countByChatRoom(chatRoom);
    }

    public boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member) {
        return companionPostChatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, member.getId());
    }

    // 사용자가 해당 채팅방에 존재하는지 확인
    @Override
    public boolean isUserInRoom(String roomId, Long userId) {
        logger.info("isUserInRoom called with roomId={}, userId={}", roomId, userId);
        String raw = stripCrPrefix(roomId);

        // roomId로 채팅방 조회
        ChatRoom chatRoom = companionPostChatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("Found chatRoom: {}", chatRoom.getRoomId());

        // 사용자가 해당 채팅방에 존재하는지 확인
        boolean exists = companionPostChatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, userId);

        logger.info("User {} is {} in the roomId {}", userId, exists ? "already" : "not", roomId);

        return exists;
    }


    @Override
    @Transactional
    public void leave(String roomId, Long meId) {
        String raw = stripCrPrefix(roomId);

        ChatRoom room = companionPostChatRoomRepository.findByRoomId(raw)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        Member me = memberRepository.findById(meId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatParticipant myPart = companionPostChatParticipantRepository.findByChatRoomAndMember(room, me);
        if (myPart != null) {
            companionPostChatParticipantRepository.delete(myPart);
        }

//        // 1명 이하만 남으면 방 상태 플래그 변경/삭제
//        int remain = companionPostChatParticipantRepository.countByChatRoom(room);
//        if (remain <= 1) {
//            // room.setStatus(RoomStatus.CLOSED);
//            companionPostChatRoomRepository.save(room);
//        }
    }

}
