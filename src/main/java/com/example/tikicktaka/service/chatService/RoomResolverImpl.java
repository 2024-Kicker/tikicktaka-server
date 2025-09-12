package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomResolverImpl implements RoomResolver {

    private final StoryRoomRepository storyRoomRepository;
    private final CompanionPostChatRoomRepository companionRoomRepository;

    @Override
    public java.util.Optional<ResolvedRoom> resolve(String roomId) {
        if (roomId == null || roomId.isBlank()) return java.util.Optional.empty();

        // 1) 접두사 빠른 분기
        if (roomId.startsWith("SR-")) {
            return storyRoomRepository.findByRoomId(roomId)
                    .map(r -> new ResolvedRoom(RoomType.STORY, r.getId(), r.getRoomId()));
        }
        if (roomId.startsWith("CR-")) {
            return companionRoomRepository.findByRoomId(roomId)
                    .map(r -> new ResolvedRoom(RoomType.COMPANION, r.getId(), r.getRoomId()));
        }

        // 2) 레거시(접두사 없음) → 양쪽 조회
        var story = storyRoomRepository.findByRoomId(roomId);
        if (story.isPresent()) {
            var r = story.get();
            return java.util.Optional.of(new ResolvedRoom(RoomType.STORY, r.getId(), r.getRoomId()));
        }
        var comp = companionRoomRepository.findByRoomId(roomId);
        if (comp.isPresent()) {
            var r = comp.get();
            return java.util.Optional.of(new ResolvedRoom(RoomType.COMPANION, r.getId(), r.getRoomId()));
        }
        return java.util.Optional.empty();
    }
}
