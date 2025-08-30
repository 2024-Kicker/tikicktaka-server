package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;

import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.blocked.Blocked;
import com.example.tikicktaka.domain.mapping.scrap.Scrap;
import com.example.tikicktaka.domain.travel.TravelRegion;

import com.example.tikicktaka.repository.blocked.BlockedRepository;
import com.example.tikicktaka.repository.scrap.ScrapRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;

import com.example.tikicktaka.service.blocked.BlockedService;
import com.example.tikicktaka.service.scrap.ScrapCommandService;
import com.example.tikicktaka.service.stadiumAttractionService.StadiumAttractionQueryService;
import com.example.tikicktaka.web.dto.myPage.BlockedTravelRegionDTO;
import com.example.tikicktaka.web.dto.travelRegion.TravelRegionScrapDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    private final BlockedRepository blockedRepository;
    private final BlockedService blockedService;

    private static final int MAX_RETURN = 200;


    @PostMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩", description = "해당 여행지를 스크랩합니다.")
    public ApiResponse<Void> scrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        if (!travelRegionRepository.existsById(travelRegionId)) {
            throw new EntityNotFoundException("여행지를 찾을 수 없습니다.");
        }

        if (scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(memberId, TargetType.TRAVEL_REGION, travelRegionId)) {
            return ApiResponse.onSuccess(null);
        }

        scrapRepository.save(Scrap.of(memberId, TargetType.TRAVEL_REGION, travelRegionId));
        return ApiResponse.onSuccess(null);
    }

    @Transactional
    @DeleteMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 취소", description = "스크랩한 여행지를 취소합니다.")
    public ApiResponse<Void> unScrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        boolean removed = scrapCommandService.removeIfOwned(memberId, TargetType.TRAVEL_REGION, travelRegionId);
        if (!removed) {
            return ApiResponse.onFailure(
                    ErrorStatus._BAD_REQUEST.getCode(),
                    "해당 여행지를 스크랩한 이력이 없습니다.",
                    null
            );
        }
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 여부", description = "해당 여행지를 스크랩했는지 여부를 반환합니다.")
    public ApiResponse<Boolean> isScrapped(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);
        boolean exists = scrapRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.TRAVEL_REGION, travelRegionId
        );
        return ApiResponse.onSuccess(exists);
    }


    @GetMapping("/scraps")
    @Operation(summary = "내 여행지 스크랩 목록(심플)", description = "파라미터 없이, 내가 스크랩한 여행지를 간단한 배열로 반환합니다.")
    public ApiResponse<List<TravelRegionScrapDTO>> mySimpleScraps(Authentication auth) {
        Long memberId = requireMemberId(auth);

        List<Long> ids = scrapRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.TRAVEL_REGION)
                .stream().map(Scrap::getTargetId).toList();

        if (ids.isEmpty()) {
            return ApiResponse.onSuccess(List.of());
        }

        List<Long> limited = ids.size() > MAX_RETURN ? ids.subList(0, MAX_RETURN) : ids;

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

    @PostMapping("/{travelRegionId}/block")
    @Operation(summary = "여행지 차단", description = "해당 여행지를 추천안받기로(차단) 등록합니다.")
    public ApiResponse<Void> block(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        if (!travelRegionRepository.existsById(travelRegionId)) {
            throw new EntityNotFoundException("여행지를 찾을 수 없습니다.");
        }

        boolean exists = blockedRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.TRAVEL_REGION, travelRegionId
        );
        if (exists) {
            // 이미 차단되어 있으면 그냥 성공 반환 (멱등)
            return ApiResponse.onSuccess(null);
        }

        blockedRepository.save(Blocked.of(memberId, TargetType.TRAVEL_REGION, travelRegionId));
        return ApiResponse.onSuccess(null);
    }

    @Transactional
    @DeleteMapping("/{travelRegionId}/block")
    @Operation(summary = "여행지 차단 해제", description = "해당 여행지의 추천안받기(차단)를 해제합니다.")
    public ApiResponse<Void> unblock(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        boolean removed = blockedService.removeIfOwned(memberId, TargetType.TRAVEL_REGION, travelRegionId);
        if (!removed) {
            return ApiResponse.onFailure(
                    ErrorStatus._BAD_REQUEST.getCode(),
                    "해당 여행지를 차단한 이력이 없습니다.",
                    null
            );
        }
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{travelRegionId}/block")
    @Operation(summary = "여행지 차단 여부", description = "해당 여행지를 추천안받기(차단)했는지 여부를 반환합니다.")
    public ApiResponse<Boolean> isBlocked(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);
        boolean exists = blockedRepository.existsByMemberIdAndTargetTypeAndTargetId(
                memberId, TargetType.TRAVEL_REGION, travelRegionId
        );
        return ApiResponse.onSuccess(exists);
    }

    @GetMapping("/blocks")
    @Operation(summary = "내 여행지 차단 목록(심플)", description = "파라미터 없이, 내가 차단한 여행지를 간단한 배열로 반환합니다.")
    public ApiResponse<List<BlockedTravelRegionDTO>> mySimpleBlocks(Authentication auth) {
        Long memberId = requireMemberId(auth);

        List<Long> ids = blockedRepository
                .findByMemberIdAndTargetTypeOrderByCreatedAtDesc(memberId, TargetType.TRAVEL_REGION)
                .stream().map(Blocked::getTargetId).toList();

        if (ids.isEmpty()) {
            return ApiResponse.onSuccess(List.of());
        }

        List<Long> limited = ids.size() > MAX_RETURN ? ids.subList(0, MAX_RETURN) : ids;

        Map<Long, TravelRegion> map = travelRegionRepository.findAllById(limited)
                .stream().collect(Collectors.toMap(TravelRegion::getId, it -> it));

        List<BlockedTravelRegionDTO> result = limited.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(r -> BlockedTravelRegionDTO.builder()
                        .id(r.getId())
                        .name(r.getTitle())
                        .address(r.getAddr1())
                        .thumbnailUrl(r.getFirstImage())
                        .blocked(true)
                        .build()
                )
                .toList();

        return ApiResponse.onSuccess(result);
    }

    private Long requireMemberId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new MemberHandler(ErrorStatus._UNAUTHORIZED);
        }
        return Long.valueOf(auth.getName());
    }
}
