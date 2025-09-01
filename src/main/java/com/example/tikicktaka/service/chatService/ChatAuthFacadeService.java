package com.example.tikicktaka.service.chatService;

public interface ChatAuthFacadeService {

    /**
     * 동행(Companion) 채팅 전용 권한 체크
     * @param meId 현재 사용자 ID
     * @param roomId 채팅방 식별자
     * @return AuthResult (authorView: 방장이면 true)
     */
    AuthResult resolveCompanion(Long meId, String roomId);

    record AuthResult(boolean authorView) {}
}
