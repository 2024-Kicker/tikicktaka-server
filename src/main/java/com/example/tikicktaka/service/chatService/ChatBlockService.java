package com.example.tikicktaka.service.chatService;


import java.util.List;

public interface ChatBlockService {
    void block(Long meId, Long targetMemberId);
    void unblock(Long meId, Long targetMemberId);
    List<Long> list(Long meId);
}

