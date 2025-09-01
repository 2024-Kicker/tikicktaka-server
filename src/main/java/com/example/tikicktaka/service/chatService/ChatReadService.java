package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatMessagePageDTO;

public interface ChatReadService {

    /**
     * 동행(Companion) 채팅방 메시지 읽기 (키셋 페이지네이션 + 차단 마스킹)
     *
     * @param meId   현재 사용자 ID
     * @param roomId 채팅방 roomId(문자)
     * @param dir    "prev" | "next" (기본 next)
     * @param cursor 키셋 커서(메시지 id), 최초 진입 시 null
     * @param size   페이지 크기
     * @return ChatMessagePageDTO
     */
    ChatMessagePageDTO readCompanionMessages(Long meId, String roomId,
                                             String dir, Long cursor, int size);
}
