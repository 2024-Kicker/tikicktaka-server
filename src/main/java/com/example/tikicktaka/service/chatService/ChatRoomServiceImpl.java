package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.companionPost.CompanionPost;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatMessageRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
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

@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final CompanionPostChatRoomRepository companionPostChatRoomRepository;
    private final CompanionPostChatParticipantRepository companionPostChatParticipantRepository;
    private final MemberRepository memberRepository;
    private final CompanionPostRepository companionPostRepository;
    private final CompanionPostChatMessageRepository companionPostChatMessageRepository;
    private final InviteCodeGeneratorService inviteCodeGeneratorService;
    private final Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);
    private final RedisService redisService;  // RedisService 객체


    @Autowired
    public ChatRoomServiceImpl(CompanionPostChatRoomRepository companionPostChatRoomRepository, CompanionPostChatParticipantRepository companionPostChatParticipantRepository,
                               MemberRepository memberRepository, CompanionPostRepository companionPostRepository, CompanionPostChatMessageRepository companionPostChatMessageRepository, InviteCodeGeneratorService inviteCodeGeneratorService, RedisService redisService) {
        this.companionPostChatRoomRepository = companionPostChatRoomRepository;
        this.companionPostChatParticipantRepository = companionPostChatParticipantRepository;
        this.memberRepository = memberRepository;
        this.companionPostRepository = companionPostRepository;
        this.companionPostChatMessageRepository = companionPostChatMessageRepository;
        this.inviteCodeGeneratorService = inviteCodeGeneratorService;
        this.redisService = redisService;
    }
//    private static final String CR_PREFIX = "CR-";
//
//    private String ensureCrPrefix(String id) {
//        if (id == null) return null;
//        return id.startsWith(CR_PREFIX) ? id : CR_PREFIX + id;
//    }
//    private String stripCrPrefix(String id) {
//        if (id == null) return null;
//        return id.startsWith(CR_PREFIX) ? id.substring(CR_PREFIX.length()) : id;
//    }


    //단체방 초대 및 생성
    @Override
    @Transactional
    public ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO) {
        Member owner = memberRepository.findById(chatRoomDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("소유자 정보를 찾을 수 없습니다."));

        CompanionPost companionPost = companionPostRepository.findById(chatRoomDTO.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글 정보를 찾을 수 없습니다."));


        // 단체방 존재 여부 정확히 검사
        boolean existsGroup = companionPostChatRoomRepository
                .existsByCompanionPost_IdAndIsGroupTrue(chatRoomDTO.getPostId());
        if (existsGroup) {
            // 이미 있으면 그 방을 리턴해도 되고, 409로 막아도 OK
            // 여기선 기존 방 DTO 반환 패턴 유지가 맞다면 이렇게:
            ChatRoom room = companionPostChatRoomRepository.findAllByCompanionPost_Id(chatRoomDTO.getPostId())
                    .stream().filter(ChatRoom::getIsGroup).findFirst()
                    .orElseThrow(); // 논리상 존재
            ChatRoomDTO dto = new ChatRoomDTO(room);
            // 반환용 roomId에 CR- 접두사 부여
            dto.setRoomId(room.getRoomId());

          return dto;
        }

        // 초대 코드 & roomId 생성
        String inviteCode = inviteCodeGeneratorService.generateInviteCode();
        String roomId = java.util.UUID.randomUUID().toString();

        // 엔티티 생성 (participant 없음, isGroup=true)
        ChatRoom chatRoom = chatRoomDTO.toEntity(owner, companionPost);
        chatRoom.setIsGroup(true);
        chatRoom.setInviteCode(inviteCode);
        chatRoom.setRoomId(roomId); // roomId 확정 세팅

        companionPostChatRoomRepository.save(chatRoom);

        //방장 자동 참가자 등록
        boolean ownerAlreadyIn = companionPostChatParticipantRepository
                .existsByChatRoomAndMemberId(chatRoom, owner.getId());
        if (!ownerAlreadyIn) {
            companionPostChatParticipantRepository.save(ChatParticipant.create(chatRoom, owner));
        }

        // Redis에 초대 코드 저장 (10분 TTL)
//        redisService.storeInviteCode(inviteCode, 10 * 60);

        ChatRoomDTO dto = new ChatRoomDTO(chatRoom);
        // 반환용 roomId에 CR- 접두사 부여
        dto.setRoomId(chatRoom.getRoomId());
        return dto;    }

    @Override
    public boolean existsByCompanionPost_Id(Long postId) {
        return companionPostChatRoomRepository.existsByCompanionPost_Id(postId);
    }


    // roomId로 채팅방 조회 (동행찾기)
    @Override
    public Optional<ChatRoomDTO> getChatRoomById(String roomId) {
//        String raw = stripCrPrefix(roomId); // 접두사 제거 후 조회

        return companionPostChatRoomRepository.findByRoomId(roomId)
                .map(room -> {
                    ChatRoomDTO dto = new ChatRoomDTO(room);
                    // 반환용 roomId에 CR- 접두사 부여
                    dto.setRoomId(room.getRoomId());
                    return dto;
                });    }

    // 초대 코드 검증 예시 메서드
    private boolean isInviteCodeValid(String inviteCode) {
        // 초대 코드가 특정 조건에 맞는지 확인하는 로직 (예시)
        return inviteCode != null && !inviteCode.trim().isEmpty();
    }

    // 초대 코드로 채팅방 참가자 추가 메서드
    @Override
    public String joinRoomByInviteCode(String inviteCode, Long userId) {
//        // Redis에서 초대 코드 만료 여부 확인
//        if (!redisService.isInviteCodeValid(inviteCode)) {
//            throw new IllegalArgumentException("초대 코드가 만료되었습니다.");
//        }

        // MySQL에서 초대 코드로 채팅방 조회
        ChatRoom chatRoom = companionPostChatRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("초대 코드가 유효하지 않습니다."));

        // 이미 해당 채팅방에 참가했는지 확인
        boolean alreadyParticipating = companionPostChatParticipantRepository.existsByChatRoomAndMemberId(chatRoom, userId);
        if (alreadyParticipating) {
            throw new IllegalArgumentException("이미 해당 채팅방에 참여하고 있습니다.");
        }

        // 방장이 참가하려는 경우, 참가자 목록에 추가하지 않음
        if (chatRoom.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("방장님은 이미 채팅방에 있습니다.");
        }

        // 채팅방에 참가자 추가
        addParticipantToChatRoom(chatRoom, userId);
        return chatRoom.getRoomId(); // CR- 반환
    }

    // 채팅방 참가자 추가 메서드
    private void addParticipantToChatRoom(ChatRoom chatRoom, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        ChatParticipant chatParticipant = ChatParticipant.create(chatRoom, member);
        companionPostChatParticipantRepository.save(chatParticipant);
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
        Optional<ChatRoom> existingChatRoom = companionPostChatRoomRepository.findByCompanionPostIdAndOwnerIdAndParticipantId(postId, ownerId, participantId);
        if (existingChatRoom.isPresent()) {
            logger.info("Found chatRoom: {}", existingChatRoom.get().getRoomId());
            return (existingChatRoom.get().getRoomId()); // 재사용도 CR-
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(java.util.UUID.randomUUID().toString())
                .owner(owner)  // 게시글 작성자 (owner)
                .participant(participant)  // 요청한 사용자 (participant)
                .isGroup(false)  // 1:1 채팅은 그룹이 아님
                .inviteCode(inviteCodeGeneratorService.generateInviteCode())
                .companionPost(companionPost)  // 게시글과 연결
                .build();

        // 채팅방 저장
        companionPostChatRoomRepository.save(chatRoom);

        // 참가자 정보 저장
        companionPostChatParticipantRepository.save(ChatParticipant.create(chatRoom, owner)); // 게시글 작성자
        companionPostChatParticipantRepository.save(ChatParticipant.create(chatRoom, participant)); // 요청한 사용자

        return (chatRoom.getRoomId()); // 신규도 CR-
    }

    // 채팅방 삭제
    @Override
    public void deleteChatRoom(String roomId) {
//        String raw = stripCrPrefix(roomId);
        Optional<ChatRoom> chatRoomOptional = companionPostChatRoomRepository.findByRoomId(roomId);

        if (chatRoomOptional.isEmpty()) {
            throw new IllegalArgumentException("Chat room not found.");
        }

        ChatRoom chatRoom = chatRoomOptional.get();

        // 채팅방에 연관된 메시지들 삭제 (옵션)
        companionPostChatMessageRepository.deleteByChatRoom(chatRoom);

        // 채팅방 삭제
        companionPostChatRoomRepository.delete(chatRoom);
    }

    // 특정 게시글에 해당하는 채팅방이 이미 존재하는지 확인
    public boolean isChatRoomExistForPost(Long postId) {
        return companionPostChatRoomRepository.existsByCompanionPost_Id(postId);
    }

    //게시글 삭제되면 채팅방도 삭제될 수 있도록 postId로 삭제
    @Override
    @Transactional
    public void deleteRoomsByPostId(Long postId) {
        List<ChatRoom> chatRooms = companionPostChatRoomRepository.findAllByCompanionPost_Id(postId);
        for (ChatRoom chatRoom : chatRooms) {
            companionPostChatParticipantRepository.deleteByChatRoom(chatRoom);  // 참여자 먼저 삭제
            companionPostChatMessageRepository.deleteByChatRoom(chatRoom);      // 메시지도 정리
            companionPostChatRoomRepository.delete(chatRoom);                   // 그 후 채팅방 삭제
            logger.info("게시글 {}에 연결된 채팅방 {} 삭제 완료", postId, chatRoom.getRoomId());
        }
    }

    @Override
    public boolean existsGroupRoomForPost(Long postId) {
        return companionPostChatRoomRepository.existsByCompanionPost_IdAndIsGroupTrue(postId);
    }


}
