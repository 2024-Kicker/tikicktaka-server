package com.example.tikicktaka.service.stadiumAttractionService;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import com.example.tikicktaka.domain.teams.Team;
import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.member.MemberTeamRepository;
import com.example.tikicktaka.repository.member.MemberTravelStyleRepository;
import com.example.tikicktaka.repository.scrap.ScrapRepository;
import com.example.tikicktaka.repository.team.TeamRepository;
import com.example.tikicktaka.repository.travel.TravelServiceRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StadiumAttractionQueryServiceImpl implements StadiumAttractionQueryService {

    private final TeamRepository teamRepository;
    private final TravelRegionRepository travelRegionRepository;
    private final ScrapRepository scrapRepository;

    // 기본값 로딩에 필요
    private final MemberRepository memberRepository;
    private final MemberTeamRepository memberTeamRepository;
    private final MemberTravelStyleRepository memberTravelStyleRepository;
    private final TravelServiceRepository travelServiceRepository;

    @Override
    public Page<StadiumAttractionResponseDTO.Item> findItems(
            Long memberId,
            Long teamId,                    // null 이면 회원 선호팀 사용
            List<Long> styleIdsOverride,    // 사용자가 보낸 스타일 id들(최대 2). null/빈값이면 회원 선호스타일 사용
            boolean useDefaultCategory,     // <- 더이상 필요없다면 무시/삭제해도 됨
            boolean myScrapOnly,
            String sort,                    // <- 입력 받아도 내부에서 기본 정렬만 적용
            Pageable pageable
    ) {
        // 0) 팀 결정: 입력 없으면 회원 선호팀
        Team team = resolveTeam(memberId, teamId);

        // 1) 스타일 → cat1 코드로 변환
        List<String> cat1Codes = resolveCat1Codes(memberId, styleIdsOverride);

        // 2) 정렬은 고정 (최신순)
        Sort fixedSort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageReq = (pageable == null || pageable.isUnpaged())
                ? PageRequest.of(0, 50, fixedSort)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        // 3) 조회 분기
        Page<TravelRegion> entityPage;
        if (myScrapOnly) {
            entityPage = findMyScrapsFiltered(memberId, team.getId(), cat1Codes, pageReq);
        } else {
            if (cat1Codes.isEmpty()) {
                entityPage = travelRegionRepository.findByTeam_Id(team.getId(), pageReq);
            } else {
                entityPage = travelRegionRepository.findByTeam_IdAndCat1In(team.getId(), cat1Codes, pageReq);
            }
        }

        // 4) DTO 변환
        return entityPage.map(r -> StadiumAttractionResponseDTO.Item.builder()
                .id(r.getId())
                .name(r.getTitle())
                .imageUrl(r.getFirstImage())
                .distanceKm(null)   // TODO: 구장 좌표 연동 시 계산
                .build());
    }

    private Team resolveTeam(Long memberId, Long teamId) {
        if (teamId != null) {
            return teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));
        }
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        var mt = memberTeamRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("회원 선호팀이 설정되어 있지 않습니다."));
        return mt.getTeam();
    }

    private List<String> resolveCat1Codes(Long memberId, List<Long> styleIdsOverride) {
        List<Long> styleIds = (styleIdsOverride != null && !styleIdsOverride.isEmpty())
                ? styleIdsOverride
                : loadMemberStyleIds(memberId);

        if (styleIds.isEmpty()) return List.of();

        return travelServiceRepository.findByTravelStyle_IdIn(styleIds)
                .stream()
                .map(TravelServiceRepository.CodeOnly::getCode)
                .toList();
    }


    private List<Long> loadMemberStyleIds(Long memberId) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return memberTravelStyleRepository.findByMember(member)
                .map(ms -> List.of(ms.getStyleOne().getId(), ms.getStyleTwo().getId()))
                .orElseGet(List::of);
    }

    private Page<TravelRegion> findMyScrapsFiltered(
            Long memberId, Long teamId, List<String> cat1Codes, Pageable pageable) {

        List<Long> scrappedIds = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.TRAVEL_REGION)
                .stream().map(Scrap::getTargetId).toList();

        if (scrappedIds.isEmpty()) return Page.empty(pageable);

        Map<Long, TravelRegion> map = new HashMap<>();
        travelRegionRepository.findAllById(scrappedIds).forEach(tr -> map.put(tr.getId(), tr));

        boolean hasCat1 = !cat1Codes.isEmpty();
        List<TravelRegion> filtered = scrappedIds.stream()
                .map(map::get).filter(Objects::nonNull)
                .filter(r -> Objects.equals(r.getTeam().getId(), teamId))
                .filter(r -> !hasCat1 || (r.getCat1() != null && cat1Codes.contains(r.getCat1())))
                .toList();

        // 최신 스크랩 순서 유지(이미 scrappedIds 가 그 순서)
        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<TravelRegion> slice = (start >= end) ? List.of() : filtered.subList(start, end);

        return new PageImpl<>(slice, pageable, total);
    }
}
