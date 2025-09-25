package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.web.dto.chat.ChatLinkedPostDTO;
import java.util.List;

public interface ChatLinkedPostService {

//사용자가 참여 중인 동행찾기 채팅방(1:1 + 단체방)을 모두 스캔 해 연계된 게시글 목록 반환
//정렬: 최근 대화 시각(마지막 메시지 timestamp) 내림차순

    List<ChatLinkedPostDTO> listMyLinkedCompanionPosts(Long memberId);
}
