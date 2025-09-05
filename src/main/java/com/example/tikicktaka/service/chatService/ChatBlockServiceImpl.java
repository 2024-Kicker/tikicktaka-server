package com.example.tikicktaka.service.chatService;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.repository.blocked.BlockedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.tikicktaka.domain.mapping.blocked.Blocked;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatBlockServiceImpl implements ChatBlockService {

    private final BlockedRepository blockedRepository;

    @Override
    @Transactional
    public void block(Long meId, Long targetMemberId) {
        if (meId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신을 차단할 수 없습니다.");
        }
        // 중복 차단 방지: 이미 있으면 무시
        boolean exists = blockedRepository.existsByMemberIdAndTargetTypeAndTargetId(meId, TargetType.MEMBER, targetMemberId);
        if (!exists) {
            blockedRepository.save(
                    Blocked.of(meId, TargetType.MEMBER, targetMemberId)
            );
        }
    }

    @Override
    @Transactional
    public void unblock(Long meId, Long targetMemberId) {
        blockedRepository.deleteByMemberIdAndTargetTypeAndTargetId(meId, TargetType.MEMBER, targetMemberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> list(Long meId) {
        return blockedRepository.findByMemberIdAndTargetTypeOrderByCreatedAtDesc(meId, TargetType.MEMBER)
                .stream()
                .map(b -> b.getTargetId())
                .toList();
    }
}
