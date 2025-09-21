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
        // 팀(구장) 기준 전체
    Page<TravelRegion> findByTeam_Id(Long teamId, Pageable pageable);

    // 팀 + 카테고리(숫자)
    Page<TravelRegion> findByTeam_IdAndContentTypeIdIn(Long teamId, List<Integer> contentTypeIds, Pageable pageable);

    Page<TravelRegion> findByTeam_IdAndCat1In(Long teamId, List<String> cat1Codes, Pageable pageable);
}
