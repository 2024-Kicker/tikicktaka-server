// src/main/java/com/example/tikicktaka/repository/travelRegion/TravelRegionRepository.java
package com.example.tikicktaka.repository.travelRegion;

import com.example.tikicktaka.domain.travel.TravelRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TravelRegionRepository extends JpaRepository<TravelRegion, Long> {

    /**
     * 팀 + (옵션) 카테고리(코드) IN 필터
     * categories 비어있으면 필터 미적용
     */
    @Query("""
        SELECT t FROM TravelRegion t
        WHERE (:teamId IS NULL OR t.team.id = :teamId)
          AND (:#{#categories == null || #categories.isEmpty()} = true
               OR CAST(t.contentTypeId AS string) IN :categories)
        """)
    Page<TravelRegion> findByTeamAndCategories(@Param("teamId") Long teamId,
                                               @Param("categories") List<String> categories,
                                               Pageable pageable);

    /**
     * 내 스크랩 + 팀 + (옵션) 카테고리
     * EXISTS 서브쿼리로 패키지 경로 의존 제거
     * categories 비어있으면 필터 미적용
     */
    @Query("""
        SELECT t FROM TravelRegion t
        WHERE EXISTS (
            SELECT 1 FROM TravelRegionScrap s
            WHERE s.travelRegion = t
              AND s.member.id = :memberId
        )
          AND (:teamId IS NULL OR t.team.id = :teamId)
          AND (:#{#categories == null || #categories.isEmpty()} = true
               OR CAST(t.contentTypeId AS string) IN :categories)
        """)
    Page<TravelRegion> findMyScrappedByTeamAndCategories(@Param("memberId") Long memberId,
                                                         @Param("teamId") Long teamId,
                                                         @Param("categories") List<String> categories,
                                                         Pageable pageable);
}
