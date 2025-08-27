// src/main/java/com/example/tikicktaka/service/stadiumAttractionService/StadiumAttractionQueryServiceImpl.java
package com.example.tikicktaka.service.stadiumAttractionService;

import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StadiumAttractionQueryServiceImpl implements StadiumAttractionQueryService {

    private final TeamRepository teamRepository;
    private final TravelRegionRepository travelRegionRepository;

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
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        List<String> categoriesToUse = categoryOverride;
        if ((categoriesToUse == null || categoriesToUse.isEmpty()) && useDefaultCategory) {
            categoriesToUse = loadDefaultCategoriesForMember(memberId); // TODO
        }

        Sort s = switch (sort) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "contentTypeId"); // 임시
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);

        Page<TravelRegion> entityPage;
        if (myScrapOnly) {
            entityPage = travelRegionRepository.findMyScrappedByTeamAndCategories(
                    memberId, teamId, categoriesToUse, sortedPageable
            );
        } else {
            entityPage = travelRegionRepository.findByTeamAndCategories(
                    teamId, categoriesToUse, sortedPageable
            );
        }

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
}
