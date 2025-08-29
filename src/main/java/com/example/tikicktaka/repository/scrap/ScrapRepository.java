package com.example.tikicktaka.repository.scrap;

import com.example.tikicktaka.domain.enums.ScrapTargetType;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    List<Scrap> findByMemberIdAndTargetTypeOrderByCreatedAtDesc(Long memberId, ScrapTargetType type);
    boolean existsByMemberIdAndTargetTypeAndTargetId(Long memberId, ScrapTargetType type, Long targetId);
    void deleteByMemberIdAndTargetTypeAndTargetId(Long memberId, ScrapTargetType type, Long targetId);
}
