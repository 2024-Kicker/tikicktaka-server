package com.example.tikicktaka.service.chatService;

import java.util.List;
import java.util.Map;

public interface MemberDisplayNameService {

    /**
     * 멤버 ID 목록을 닉네임으로 매핑
     * @param memberIds 멤버 ID 리스트
     * @return (memberId -> 닉네임) Map
     */
    Map<Long, String> namesOf(List<Long> memberIds);
}
