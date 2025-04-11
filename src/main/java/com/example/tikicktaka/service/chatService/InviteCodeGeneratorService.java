package com.example.tikicktaka.service.chatService;

public interface InviteCodeGeneratorService {
    String generateInviteCode(); // 초대 코드 생성 메서드

    boolean isInviteCodeValid(String inviteCode);
}

