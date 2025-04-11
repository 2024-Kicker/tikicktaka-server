package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.config.SocketIOConfig;
import com.example.tikicktaka.domain.chat.ChatParticipant;
import com.example.tikicktaka.domain.chat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.chat.ChatParticipantRepository;
import com.example.tikicktaka.repository.chat.ChatRoomRepository;
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

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final Logger logger = LoggerFactory.getLogger(SocketIOConfig.class);


    @Override
    public void addParticipant(String roomId, Long userId) {
        logger.info("🛠️ addParticipant called with roomId={}, userId={}", roomId, userId);

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("✅ Found chatRoom: {}", chatRoom.getRoomId());

        // userId로 회원(Member) 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        logger.info("✅ Found member: {}", member.getId());

        // ChatParticipant 객체 생성
        ChatParticipant participant = ChatParticipant.create(chatRoom, member);
        chatParticipantRepository.save(participant);

        logger.info("✅ Successfully added participant: userId={} to roomId={}", userId, roomId);
    }

    // 채팅방에서 참가자 제거
    @Override
    @Transactional
    public void removeParticipant(String roomId, Member member) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("🧹 Removing participant: memberId={} from roomId={}", member.getId(), roomId);

        // 현재 멤버 제거
        chatParticipantRepository.deleteByChatRoomAndMember(chatRoom, member);

        // 남은 참가자 수 확인
        int remainingCount = chatParticipantRepository.countByChatRoom(chatRoom);
        logger.info("👥 Remaining participants in room {}: {}", roomId, remainingCount);

        // 참가자가 한 명 이하라면 채팅방 삭제
        if (remainingCount <= 1) {
            // 남은 참가자 전체 제거
            List<ChatParticipant> remainingParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
            chatParticipantRepository.deleteAll(remainingParticipants);

            // 채팅방 삭제
            chatRoomRepository.delete(chatRoom);
            logger.info("🗑️ Deleted chat room {} because it only had one or no participants", roomId);
        }
    }

    @Override
    public List<ChatParticipant> getParticipants(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        return chatParticipantRepository.findByChatRoom(chatRoom);
    }

    // 채팅방 참가자 수 확인
    @Override
    public int getParticipantCount(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        return chatParticipantRepository.countByChatRoom(chatRoom);
    }

    public boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member) {
        return chatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, member.getId());
    }

    // 사용자가 해당 채팅방에 존재하는지 확인
    @Override
    public boolean isUserInRoom(String roomId, Long userId) {
        logger.info("🛠️ isUserInRoom called with roomId={}, userId={}", roomId, userId);

        // roomId로 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        logger.info("✅ Found chatRoom: {}", chatRoom.getRoomId());

        // 사용자가 해당 채팅방에 존재하는지 확인
        boolean exists = chatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, userId);

        logger.info("✅ User {} is {} in the roomId {}", userId, exists ? "already" : "not", roomId);

        return exists;
    }

}
