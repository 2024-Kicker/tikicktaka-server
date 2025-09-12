package com.example.tikicktaka.service.storyChat;

import com.example.tikicktaka.repository.storyRoom.StoryRoomRepository;
import com.example.tikicktaka.service.storyChat.StoryChatAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryChatAuthServiceImpl implements StoryChatAuthService {
    private final StoryRoomRepository storyRoomRepository;
    @Override
    public void ensureEnterable(Long storyRoomPk, Long memberId) {
        var room = storyRoomRepository.findById(storyRoomPk)
                .orElseThrow(() -> new IllegalArgumentException("이야기방 없음"));
        // 추후 마감/차단/참여자 여부 체크 연결
    }
}
