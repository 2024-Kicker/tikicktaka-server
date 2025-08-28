// src/main/java/com/example/tikicktaka/service/stadiumAttractionService/StadiumAttractionQueryServiceImpl.java
package com.example.tikicktaka.service.stadiumAttractionService;

import com.example.tikicktaka.domain.mapping.scrap.TravelRegionScrap;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionScrapRepository;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StadiumAttractionQueryServiceImpl implements StadiumAttractionQueryService {

    private final TeamRepository teamRepository;
    private final TravelRegionRepository travelRegionRepository;
    private final TravelRegionScrapRepository scrapRepository; // ✅ 추가

    @Override
    public Page<StadiumAttractionResponseDTO.Item> findItems(
            Long memberId,
            Long teamId,
            List<String> categoryOverride,
            boolean useDefaultCategory,
            boolean myScrapOnly,
            String sort,
            Pageable pageable
    ) {
        // 0) 팀 존재 검증 (구장 좌표/필터 용)
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // 1) 카테고리 결정
        List<String> categoriesToUse = categoryOverride;
        if ((categoriesToUse == null || categoriesToUse.isEmpty()) && useDefaultCategory) {
            categoriesToUse = loadDefaultCategoriesForMember(memberId); // TODO: 마이페이지 연동
        }
        List<Integer> categoryIds = toIntegerList(categoriesToUse);

        // 2) 정렬 옵션
        Sort s = switch (sort) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "contentTypeId"); // 임시: 별도 평점 필드 생기면 교체
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);

        // 3) 조회 분기
        Page<TravelRegion> entityPage;
        if (myScrapOnly) {
            // 스크랩 전용 경로는 ScrapRepository 네이밍 메서드만 사용 (JPQL 없음)
            Page<TravelRegionScrap> scrapPage;
            boolean hasTeam = teamId != null;
            boolean hasCategory = !categoryIds.isEmpty();

            if (hasTeam && hasCategory) {
                scrapPage = scrapRepository.findByMemberIdAndTravelRegion_Team_IdAndTravelRegion_ContentTypeIdIn(
                        memberId, teamId, categoryIds, sortedPageable);
            } else if (hasTeam) {
                scrapPage = scrapRepository.findByMemberIdAndTravelRegion_Team_Id(memberId, teamId, sortedPageable);
            } else if (hasCategory) {
                scrapPage = scrapRepository.findByMemberIdAndTravelRegion_ContentTypeIdIn(memberId, categoryIds, sortedPageable);
            } else {
                scrapPage = scrapRepository.findByMemberId(memberId, sortedPageable);
            }

            entityPage = scrapPage.map(TravelRegionScrap::getTravelRegion);
        } else {
            // 일반 목록: TravelRegionRepository 네이밍 메서드만 사용
            boolean hasCategory = !categoryIds.isEmpty();
            if (hasCategory) {
                entityPage = travelRegionRepository.findByTeam_IdAndContentTypeIdIn(teamId, categoryIds, sortedPageable);
            } else {
                entityPage = travelRegionRepository.findByTeam_Id(teamId, sortedPageable);
            }
        }

        // 4) DTO 변환 (거리/좌표 계산은 추후 연동)
        return entityPage.map(r -> StadiumAttractionResponseDTO.Item.builder()
                .id(r.getId())
                .name(r.getTitle())
                .imageUrl(r.getFirstImage())
                .distanceKm(null) // TODO: 팀/구장 좌표 준비 시 r.mapY/mapX로 계산
                .build());
    }

    private List<String> loadDefaultCategoriesForMember(Long memberId) {
        return List.of(); // TODO: 마이페이지 저장값 연동
    }

    /** "12","76" 같은 문자열 목록을 Integer 목록으로 안전 변환(비어있거나 숫자 아님은 무시) */
    private List<Integer> toIntegerList(List<String> sources) {
        List<Integer> out = new ArrayList<>();
        if (sources == null) return out;
        for (String s : sources) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty()) continue;
            try {
                out.add(Integer.valueOf(t));
            } catch (NumberFormatException ignore) {
                // 무시
            }
        }
        return out;
    }
}
