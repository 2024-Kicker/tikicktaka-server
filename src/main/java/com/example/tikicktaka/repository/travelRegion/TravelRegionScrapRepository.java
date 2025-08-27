package com.example.tikicktaka.repository.travelRegion;

import com.example.tikicktaka.domain.mapping.scrap.TravelRegionScrap;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.travel.TravelRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelRegionScrapRepository extends JpaRepository<TravelRegionScrap, Long> {
    Optional<TravelRegionScrap> findByMemberAndTravelRegion(Member member, TravelRegion region);
    boolean existsByMemberAndTravelRegion(Member member, TravelRegion region);
    long countByTravelRegion(TravelRegion region);
}
