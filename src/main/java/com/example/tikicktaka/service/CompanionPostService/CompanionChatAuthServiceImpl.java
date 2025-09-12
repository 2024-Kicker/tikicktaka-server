package com.example.tikicktaka.service.CompanionPostService;

import com.example.tikicktaka.repository.companionPostChat.CompanionPostChatRoomRepository;
import com.example.tikicktaka.service.CompanionPostService.CompanionChatAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanionChatAuthServiceImpl implements CompanionChatAuthService {
    private final CompanionPostChatRoomRepository roomRepository;
    @Override
    public void ensureEnterable(Long companionRoomPk, Long memberId) {
        var room = roomRepository.findById(companionRoomPk)
                .orElseThrow(() -> new IllegalArgumentException("동행 채팅방 없음"));
        // 추후에: 방 상태, 초대코드/참여자 여부, 차단 체크 연결
    }
}
