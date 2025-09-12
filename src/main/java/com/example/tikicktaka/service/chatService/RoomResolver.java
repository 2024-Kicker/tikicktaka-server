package com.example.tikicktaka.service.chatService;
import java.util.Optional;

public interface RoomResolver {

    enum RoomType { STORY, COMPANION }

    record ResolvedRoom(RoomType type, Long roomPk, String roomId) {}

    Optional<ResolvedRoom> resolve(String roomId);
}
