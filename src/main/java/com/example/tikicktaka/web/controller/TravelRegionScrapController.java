package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;

import com.example.tikicktaka.domain.enums.ScrapTargetType;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import com.example.tikicktaka.domain.travel.TravelRegion;

import com.example.tikicktaka.repository.scrap.ScrapRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;

import com.example.tikicktaka.service.scrap.ScrapCommandService;
import com.example.tikicktaka.service.stadiumAttractionService.StadiumAttractionQueryService;
import com.example.tikicktaka.web.dto.travelRegion.TravelRegionScrapDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.tikicktaka.domain.enums.ScrapTargetType;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "TravelRegion scrap", description = "여행지 스크랩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel/regions")
public class TravelRegionScrapController {

    private final ScrapRepository scrapRepository;
    private final TravelRegionRepository travelRegionRepository;

    private final StadiumAttractionQueryService attractionQueryService;
    private final ScrapCommandService scrapCommandService;


    private static final int MAX_RETURN = 200;

    // 스크랩 추가 (통합 scrap)
    @PostMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩", description = "해당 여행지를 스크랩합니다.")
    public ApiResponse<Void> scrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        // 대상 존재 검증
        if (!travelRegionRepository.existsById(travelRegionId)) {
            throw new EntityNotFoundException("여행지를 찾을 수 없습니다.");
        }

        // 멱등 처리
        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, ScrapTargetType.TRAVEL_REGION, travelRegionId)) {
            return ApiResponse.onSuccess(null);
        }

        scrapRepository.save(Scrap.of(memberId, ScrapTargetType.TRAVEL_REGION, travelRegionId));
        return ApiResponse.onSuccess(null);
    }

    // 스크랩 취소 (통합 scrap)
    @Transactional
    @DeleteMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 취소", description = "스크랩한 여행지를 취소합니다.")
    public ApiResponse<Void> unScrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        boolean removed = scrapCommandService.removeIfOwned(memberId, ScrapTargetType.TRAVEL_REGION, travelRegionId);
        if (!removed) {
            return ApiResponse.onFailure(
                    ErrorStatus._BAD_REQUEST.getCode(),
                    "해당 여행지를 스크랩한 이력이 없습니다.",
                    null
            );
        }
        return ApiResponse.onSuccess(null);
    }

    // 스크랩 여부 (통합 scrap)
    @GetMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 여부", description = "해당 여행지를 스크랩했는지 여부를 반환합니다.")
    public ApiResponse<Boolean> isScrapped(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);
        boolean exists = scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, ScrapTargetType.TRAVEL_REGION, travelRegionId
        );
        return ApiResponse.onSuccess(exists);
    }

    /**
     * [내 여행지 스크랩 전체 목록] - 비페이징/무파라미터
     * - 응답량 폭증 방지 위해 서버에서 최대 개수 제한(MAX_RETURN).
     * - 통합 scrap 테이블에서 내가 스크랩한 TRAVEL_REGION을 최신순으로 조회한 뒤,
     *   ID 목록 순서를 그대로 보존해 매핑합니다.
     */
    @GetMapping("/scraps")
    @Operation(summary = "내 여행지 스크랩 목록(심플)", description = "파라미터 없이, 내가 스크랩한 여행지를 간단한 배열로 반환합니다.")
    public ApiResponse<List<TravelRegionScrapDTO>> mySimpleScraps(Authentication auth) {
        Long memberId = requireMemberId(auth);

        // 통합 scrap에서 내가 스크랩한 여행지 ID 최신순
        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, ScrapTargetType.TRAVEL_REGION)
                .stream().map(Scrap::getTargetId).toList();

        if (ids.isEmpty()) {
            return ApiResponse.onSuccess(List.of());
        }

        // 안전 상한 적용
        List<Long> limited = ids.size() > MAX_RETURN ? ids.subList(0, MAX_RETURN) : ids;

        // 대상 일괄 로드 후, 스크랩 순서대로 정렬
        Map<Long, TravelRegion> map = travelRegionRepository.findAllById(limited).stream()
                .collect(Collectors.toMap(TravelRegion::getId, it -> it));

        List<TravelRegionScrapDTO> result = limited.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(r -> TravelRegionScrapDTO.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .imageUrl(r.getFirstImage())
                        .scrapped(true)
                        .build())
                .toList();

        return ApiResponse.onSuccess(result);
    }

    // 인증에서 memberId(Long) 뽑고, 없으면 401 응답
    private Long requireMemberId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new MemberHandler(ErrorStatus._UNAUTHORIZED);
        }
        return Long.valueOf(auth.getName());
    }
}
