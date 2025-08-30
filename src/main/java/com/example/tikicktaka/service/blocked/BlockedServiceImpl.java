package com.example.tikicktaka.service.blocked;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.blocked.Blocked;
import com.example.tikicktaka.repository.blocked.BlockedRepository;
import com.example.tikicktaka.service.blocked.BlockedService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockedServiceImpl implements BlockedService {

    private final BlockedRepository repo;

    @Override
    public boolean toggle(Long memberId, TargetType type, Long targetId) {
        if (repo.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            repo.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
            return false; // 해제됨
        }
        try {
            repo.save(Blocked.of(memberId, type, targetId));
        } catch (DataIntegrityViolationException ignore) {
            // 동시 중복 요청 방어
        }
        return true; // 차단됨
    }

    @Override
    public void ensureOn(Long memberId, TargetType type, Long targetId) {
        if (!repo.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            try {
                repo.save(Blocked.of(memberId, type, targetId));
            } catch (DataIntegrityViolationException ignore) {}
        }
    }

    @Override
    public void ensureOff(Long memberId, TargetType type, Long targetId) {
        if (repo.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            repo.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> blockedIds(Long memberId, TargetType type) {
        return repo.findTargetIds(memberId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(Long memberId, TargetType type, Long targetId) {
        return repo.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
    }

    @Override
    public boolean removeIfOwned(Long memberId, TargetType type, Long targetId) {
        if (!repo.existsByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId)) {
            return false;
        }
        repo.deleteByMemberIdAndTargetTypeAndTargetId(memberId, type, targetId);
        return true;
    }
}
