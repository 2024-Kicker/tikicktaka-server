package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.chat.ChatRoom;
import com.example.tikicktaka.domain.chat.ChatParticipant;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.chat.ChatMessageRepository;
import com.example.tikicktaka.repository.chat.ChatParticipantRepository;
import com.example.tikicktaka.repository.chat.ChatRoomRepository;
import com.example.tikicktaka.repository.companionPost.CompanionPostRepository;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.RedisService;
import com.example.tikicktaka.web.dto.chat.ChatRoomDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;
    private final CompanionPostRepository companionPostRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final InviteCodeGeneratorService inviteCodeGeneratorService;
    private final Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    private final RedisService redisService;  // RedisService 객체




    @Autowired
    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository,
                               MemberRepository memberRepository, CompanionPostRepository companionPostRepository, ChatMessageRepository chatMessageRepository, InviteCodeGeneratorService inviteCodeGeneratorService, RedisService redisService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.memberRepository = memberRepository;
        this.companionPostRepository = companionPostRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.inviteCodeGeneratorService = inviteCodeGeneratorService;
        this.redisService = redisService;
    }

    //단체방 초대 및 생성
    @Override
    public ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO) {
        Member owner = memberRepository.findById(chatRoomDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("소유자 정보를 찾을 수 없습니다."));

        CompanionPost companionPost = companionPostRepository.findById(chatRoomDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글 정보를 찾을 수 없습니다."));


        // 기존 채팅방이 있는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByCompanionPost_IdAndParticipant_Id(chatRoomDTO.getPostId(), chatRoomDTO.getOwnerId());
        if (existingChatRoom.isPresent()) {
            // 이미 존재하면 해당 채팅방 반환
            return new ChatRoomDTO(existingChatRoom.get());
        }

        // 초대 코드 생성
        String inviteCode = inviteCodeGeneratorService.generateInviteCode(); // 초대 코드 생성

        // 채팅방 생성
        // ChatRoomDTO를 ChatRoom 엔티티로 변환
        ChatRoom chatRoom = chatRoomDTO.toEntity(owner, companionPost);
        chatRoom.setIsGroup(true);
        chatRoom.setInviteCode(inviteCode);  // 생성된 초대 코드 설정

        // 채팅방 저장
        chatRoomRepository.save(chatRoom);

        // Redis에 초대 코드 저장 (10분 만료)
        redisService.storeInviteCode(inviteCode, 10 * 60); // Redis에 초대 코드와 만료 시간 저장

        return new ChatRoomDTO(chatRoom); // 생성된 채팅방 반환
    }

    @Override
    public boolean existsByCompanionPost_Id(Long postId) {
        return chatRoomRepository.existsByCompanionPost_Id(postId);
    }


    // roomId로 채팅방 조회
    @Override
    public Optional<ChatRoomDTO> getChatRoomById(String roomId) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomId(roomId);
        return chatRoom.map(ChatRoomDTO::new);
    }

    // 초대 코드 검증 예시 메서드
    private boolean isInviteCodeValid(String inviteCode) {
        // 초대 코드가 특정 조건에 맞는지 확인하는 로직 (예시)
        return inviteCode != null && !inviteCode.trim().isEmpty();
    }

    // 초대 코드로 채팅방 참가자 추가 메서드
    @Override
    public String joinRoomByInviteCode(String inviteCode, Long userId) {
        // Redis에서 초대 코드 만료 여부 확인
        if (!redisService.isInviteCodeValid(inviteCode)) {
            throw new IllegalArgumentException("초대 코드가 만료되었습니다.");
        }

        // MySQL에서 초대 코드로 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("초대 코드가 유효하지 않습니다."));

        // 이미 해당 채팅방에 참가했는지 확인
        boolean alreadyParticipating = chatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, userId);
        if (alreadyParticipating) {
            throw new IllegalArgumentException("이미 해당 채팅방에 참여하고 있습니다.");
        }

        // 방장이 참가하려는 경우, 참가자 목록에 추가하지 않음
        if (chatRoom.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("방장님은 이미 채팅방에 있습니다.");
        }

        // 채팅방에 참가자 추가
        addParticipantToChatRoom(chatRoom, userId);
        return chatRoom.getRoomId();
    }

    // 채팅방 참가자 추가 메서드
    private void addParticipantToChatRoom(ChatRoom chatRoom, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        ChatParticipant chatParticipant = ChatParticipant.create(chatRoom, member);
        chatParticipantRepository.save(chatParticipant);
    }

    ///1대1 채팅방
    @Override
    public String createOneOnOneChatRoom(Long ownerId, Long participantId, Long postId) {

        // 사용자 조회
        Optional<Member> ownerOptional = memberRepository.findById(ownerId); //게시글 작성자
        Optional<Member> participantOptional = memberRepository.findById(participantId); // 일대일 채팅 요청자

        if (ownerOptional.isEmpty() || participantOptional.isEmpty()) {
            throw new IllegalArgumentException("One of the users not found.");
        }

        Member owner = ownerOptional.get();
        Member participant = participantOptional.get();

        // 게시글 조회
        CompanionPost companionPost = companionPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 게시글 작성자라면 채팅방을 생성할 수 없도록 처리
        if (participant.getId().equals(companionPost.getAuthor().getId())) {
            throw new IllegalArgumentException("자신이 작성한 게시글에 대한 1:1 채팅방을 생성할 수 없습니다.");
        }

        // 1:1 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByCompanionPostIdAndOwnerIdAndParticipantId(postId, ownerId, participantId);
        if (existingChatRoom.isPresent()) {
            logger.info("✅ Found chatRoom: {}", existingChatRoom.get().getRoomId());
            return existingChatRoom.get().getRoomId();
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .owner(owner)  // 게시글 작성자 (owner)
                .participant(participant)  // 요청한 사용자 (participant)
                .isGroup(false)  // 1:1 채팅은 그룹이 아님
                .companionPost(companionPost)  // 게시글과 연결
                .build();

        // 채팅방 저장
        chatRoomRepository.save(chatRoom);

        // 참가자 정보 저장
        chatParticipantRepository.save(ChatParticipant.create(chatRoom, owner)); // 게시글 작성자
        chatParticipantRepository.save(ChatParticipant.create(chatRoom, participant)); // 요청한 사용자

        return chatRoom.getRoomId();  // 새로 생성된 방 ID 반환
    }


//    // 1:1 채팅방 생성시 방 ID 생성 방법
//    private String generateOneOnOneRoomId(Long userId, Long targetUserId) {
//        // 두 사용자 ID를 합쳐서 고유한 roomId 생성 (예: "userId-targetUserId" 또는 "targetUserId-userId")
//        return userId < targetUserId ? userId + "-" + targetUserId : targetUserId + "-" + userId;
//    }

    // 채팅방 삭제
    @Override
    public void deleteChatRoom(String roomId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByRoomId(roomId);

        if (chatRoomOptional.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found.");
        }

        ChatRoom chatRoom = chatRoomOptional.get();

        // 채팅방에 연관된 메시지들 삭제 (옵션)
        chatMessageRepository.deleteByChatRoom(chatRoom);

        // 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
    }

    // 특정 게시글에 해당하는 채팅방이 이미 존재하는지 확인
    public boolean isChatRoomExistForPost(Long postId) {
        return chatRoomRepository.existsByCompanionPost_Id(postId);
    }

    //게시글 삭제되면 채팅방도 삭제될 수 있도록 postId로 삭제
    @Override
    @Transactional
    public void deleteRoomsByPostId(Long postId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByCompanionPost_Id(postId);
        for (ChatRoom chatRoom : chatRooms) {
            chatParticipantRepository.deleteByChatRoom(chatRoom);  // 참여자 먼저 삭제
            chatMessageRepository.deleteByChatRoom(chatRoom);      // 메시지도 정리
            chatRoomRepository.delete(chatRoom);                   // 그 후 채팅방 삭제
            logger.info("🗑️ 게시글 {}에 연결된 채팅방 {} 삭제 완료", postId, chatRoom.getRoomId());
        }
    }


}
