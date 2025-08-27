package com.example.tikicktaka.repository.stadiumAttraction;

import com.example.tikicktaka.domain.travel.TravelLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StadiumAttractionRepository {

    /**
     * 구장별 관광지 조회
     * @param teamId   팀(구장) ID
     * @param lat      기준 위도 (거리 계산 시 사용)
     * @param lng      기준 경도 (거리 계산 시 사용)
     * @param sort     정렬 기준 (popularity, rating, distance)
     * @param category 카테고리 필터
     * @param pageable 페이징 정보
     */
    Page<TravelLocation> searchByTeam(Long teamId, Double lat, Double lng, String sort, String category, Pageable pageable);
}
