package com.example.tikicktaka.service.blocked;

import com.example.tikicktaka.domain.enums.TargetType;

import java.util.List;

public interface BlockedService {

    /**
     * 차단 토글: 이미 차단되어 있으면 해제, 아니면 차단
     * @return true = 차단됨, false = 해제됨
     */
    boolean toggle(Long memberId, TargetType type, Long targetId);

    /** 명시적으로 차단 ON (중복이면 무시) */
    void ensureOn(Long memberId, TargetType type, Long targetId);

    /** 명시적으로 차단 OFF (없으면 무시) */
    void ensureOff(Long memberId, TargetType type, Long targetId);

    /** 특정 타입의 차단 대상 id 목록 */
    List<Long> blockedIds(Long memberId, TargetType type);

    /** 특정 대상이 차단 상태인지 여부 */
    boolean isBlocked(Long memberId, TargetType type, Long targetId);
}
