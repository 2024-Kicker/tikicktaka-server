package com.example.tikicktaka.repository.travelRegion;

import com.example.tikicktaka.domain.mapping.scrap.TravelRegionScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelRegionScrapRepository extends JpaRepository<TravelRegionScrap, Long> {

    //특정 회원이 특정 여행지를 스크랩했는지 여부
    boolean existsByMemberIdAndTravelRegionId(Long memberId, Long travelRegionId);

    //특정 회원의 특정 여행지 스크랩 엔티티 조회
    Optional<TravelRegionScrap> findByMemberIdAndTravelRegionId(Long memberId, Long travelRegionId);

    //특정 회원의 특정 여행지 스크랩 삭제
    void deleteByMemberIdAndTravelRegionId(Long memberId, Long travelRegionId);

    //특정 회원의 모든 스크랩 목록 (페이지)
    Page<TravelRegionScrap> findByMemberId(Long memberId, Pageable pageable);

    //특정 회원 + 팀(구장) 기준 스크랩 목록 (페이지)
    Page<TravelRegionScrap> findByMemberIdAndTravelRegion_Team_Id(Long memberId, Long teamId, Pageable pageable);

    //특정 회원 + 카테고리(contentTypeId) 목록 기준 스크랩 (페이지)
    Page<TravelRegionScrap> findByMemberIdAndTravelRegion_ContentTypeIdIn(Long memberId, List<Integer> contentTypeIds, Pageable pageable);

    //특정 회원 + 팀 + 카테고리 동시 필터 스크랩 (페이지)
    Page<TravelRegionScrap> findByMemberIdAndTravelRegion_Team_IdAndTravelRegion_ContentTypeIdIn(
            Long memberId, Long teamId, List<Integer> contentTypeIds, Pageable pageable
    );

    List<com.example.tikicktaka.domain.mapping.scrap.TravelRegionScrap>
    findAllByMemberIdOrderByIdDesc(Long memberId);

// 스크랩 시각 기준 정렬을 원하면 엔티티에 createdAt 필드가 있을 때:
// List<TravelRegionScrap> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 특정 여행지의 총 스크랩 수
    long countByTravelRegionId(Long travelRegionId);
}
