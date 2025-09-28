// src/main/java/com/example/tikicktaka/service/chatService/RoomOthersService.java
package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.RoomParticipantsResponseDTO;

public interface RoomOthersService {
    enum RoomType { COMPANION, STORY }

    // type + roomId(문자열) 명시 호출 (권장)
    RoomOthersServiceImpl.Result getOthers(RoomType type, String roomId, Long meId);

    // type 없이 roomId만 → RoomResolver로 자동 판별
    RoomOthersServiceImpl.Result getOthersAuto(String roomId, Long meId);
}
