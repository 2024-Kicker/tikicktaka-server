package com.example.tikicktaka.repository.blocked;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.blocked.Blocked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlockedRepository extends JpaRepository<Blocked, Long> {

    boolean existsByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType type, Long targetId);

    void deleteByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType type, Long targetId);

    List<Blocked> findByMemberIdAndTargetTypeOrderByCreatedAtDesc(Long memberId, TargetType type);

    @Query("select b.targetId from Blocked b where b.memberId = :memberId and b.targetType = :type")
    List<Long> findTargetIds(Long memberId, TargetType type);
}
