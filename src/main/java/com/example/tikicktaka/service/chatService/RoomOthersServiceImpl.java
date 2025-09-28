package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.companionPostChat.ChatParticipant;
import com.example.tikicktaka.domain.companionPostChat.ChatRoom;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.storyRoom.StoryRoom;
import com.example.tikicktaka.domain.storyRoom.StoryRoomParticipant;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatParticipantRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomParticipantRepository;
import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.web.dto.chat.ChatParticipantDTO;
import com.example.tikicktaka.web.dto.chat.RoomParticipantsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomOthersServiceImpl implements RoomOthersService {

    private final CompanionPostChatRoomRepository companionRoomRepo;
    private final CompanionPostChatParticipantRepository companionPartRepo;
    private final StoryRoomRepository storyRoomRepo;
    private final StoryRoomParticipantRepository storyPartRepo;
    private final RoomResolver roomResolver;

    // 설정 없으면 S3의 대체 이미지를 사용 (원하면 application.yml에 member.default-profile 추가)
    @Value("${member.default-profile:https://tikicktaka-bucket.s3.ap-northeast-2.amazonaws.com/logo/Companion_Travel.png}")
    private String defaultProfileUrl;

    public record Result(RoomParticipantsResponseDTO dto) {}

    @Override
    public Result getOthers(RoomType type, String roomId, Long meId) {
        return switch (type) {
            case COMPANION -> buildForCompanion(roomId, meId);
            case STORY     -> buildForStory(roomId, meId);
        };
    }

    @Override
    public Result getOthersAuto(String roomId, Long meId) {
        var resolved = roomResolver.resolve(roomId)
                .orElseThrow(() -> new IllegalArgumentException("roomId를 해석할 수 없습니다: " + roomId));
        return getOthers(
                resolved.type() == RoomResolver.RoomType.COMPANION ? RoomType.COMPANION : RoomType.STORY,
                resolved.roomId(),
                meId
        );
    }

    private Result buildForCompanion(String roomId, Long meId) {
        ChatRoom room = companionRoomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        List<ChatParticipantDTO> others = companionPartRepo.findByChatRoom(room).stream()
                .map(ChatParticipant::getMember)
                .filter(m -> !m.getId().equals(meId))
                .map(this::toBrief)
                .collect(Collectors.toList());

        var dto = RoomParticipantsResponseDTO.builder()
                .type(RoomType.COMPANION.name())
                .roomId(roomId)
                .participants(others)
                .build();
        return new Result(dto);
    }

    private Result buildForStory(String roomId, Long meId) {
        StoryRoom room = storyRoomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("이야기방을 찾을 수 없습니다: " + roomId));

        List<ChatParticipantDTO> others = storyPartRepo.findByStoryRoom(room).stream()
                .map(StoryRoomParticipant::getMember)
                .filter(m -> !m.getId().equals(meId))
                .map(this::toBrief)
                .collect(Collectors.toList());

        var dto = RoomParticipantsResponseDTO.builder()
                .type(RoomType.STORY.name())
                .roomId(roomId)
                .participants(others)
                .build();
        return new Result(dto);
    }

    private ChatParticipantDTO toBrief(Member m) {
        String nick = nullSafe(mName(m));
        if (!StringUtils.hasText(nick)) nick = "user#" + m.getId();

        String img = (m.getProfileImg() != null && StringUtils.hasText(m.getProfileImg().getUrl()))
                ? m.getProfileImg().getUrl()
                : defaultProfileUrl;

        return ChatParticipantDTO.builder()
                .memberId(m.getId())
                .name(nick)
                .profileUrl(img)
                .build();
    }

    // 프로젝트에 닉네임 필드 없으면 getName()으로 바꿔도 됨
    private String mName(Member m) {
        try {
            var method = m.getClass().getMethod("getName");
            Object val = method.invoke(m);
            return val == null ? null : val.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
