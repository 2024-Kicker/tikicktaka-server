package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.domain.mapping.scrap.TravelRegionScrap;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.travel.TravelRegion;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionRepository;
import com.example.tikicktaka.repository.travelRegion.TravelRegionScrapRepository;
import com.example.tikicktaka.service.stadiumAttractionService.StadiumAttractionQueryService;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import com.example.tikicktaka.web.dto.travelRegion.TravelRegionScrapDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "TravelRegion Scrap", description = "여행지 스크랩 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/travel/regions")

public class TravelRegionScrapController {

    private final TravelRegionScrapRepository scrapRepository;
    private final TravelRegionRepository travelRegionRepository;
    private final MemberRepository memberRepository;
    private final StadiumAttractionQueryService attractionQueryService;
    private static final int MAX_RETURN = 200;

    // 스크랩 추가
    @PostMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩", description = "해당 여행지를 스크랩합니다.")
    public ApiResponse<Void> scrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);

        // 중복 방지
        if (scrapRepository.existsByMemberIdAndTravelRegionId(memberId, travelRegionId)) {
            return ApiResponse.onSuccess(null); // 이미 스크랩이면 멱등 처리
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        TravelRegion region = travelRegionRepository.findById(travelRegionId)
                .orElseThrow(() -> new EntityNotFoundException("여행지를 찾을 수 없습니다."));

        scrapRepository.save(TravelRegionScrap.builder()
                .member(member)
                .travelRegion(region)
                .build());

        return ApiResponse.onSuccess(null);
    }

    //스크랩 취소
    @DeleteMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 취소", description = "스크랩한 여행지를 취소합니다.")
    public ApiResponse<Void> unScrap(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);
        scrapRepository.findByMemberIdAndTravelRegionId(memberId, travelRegionId)
                .ifPresent(scrapRepository::delete);
        return ApiResponse.onSuccess(null);
    }

    // 여행지 스크랩 여부
    @GetMapping("/{travelRegionId}/scrap")
    @Operation(summary = "여행지 스크랩 여부", description = "해당 여행지를 스크랩했는지 여부를 반환합니다.")
    public ApiResponse<Boolean> isScrapped(@PathVariable Long travelRegionId, Authentication auth) {
        Long memberId = requireMemberId(auth);
        boolean exists = scrapRepository.existsByMemberIdAndTravelRegionId(memberId, travelRegionId);
        return ApiResponse.onSuccess(exists);
    }

    /**
     * [내 여행지 스크랩 전체 목록] - 비페이징/무파라미터
     * - 이야기방 응답 형태처럼 간단한 배열로 반환합니다.
     * - 응답량 폭증을 방지하기 위해 서버에서 최대 개수를 제한(MAX_RETURN)합니다.
     */
    @GetMapping("/scraps")
    @Operation(summary = "내 여행지 스크랩 목록(심플)", description = "파라미터 없이, 내가 스크랩한 여행지를 간단한 배열로 반환합니다.")
    public ApiResponse<List<TravelRegionScrapDTO>> mySimpleScraps(Authentication auth) {
        Long memberId = requireMemberId(auth);

        // 비페이징 조회
        List<TravelRegionScrap> scraps = scrapRepository.findAllByMemberIdOrderByIdDesc(memberId);

        // 안전 상한 적용
        if (scraps.size() > MAX_RETURN) {
            scraps = scraps.subList(0, MAX_RETURN);
        }

        // 매핑
        List<TravelRegionScrapDTO> result = scraps.stream()
                .map(s -> {
                    var r = s.getTravelRegion();
                    return TravelRegionScrapDTO.builder()
                            .id(r.getId())
                            .title(r.getTitle())
                            .imageUrl(r.getFirstImage())
                            .scrapped(true)
                            //.categoryCode(r.getContentTypeId() != null ? r.getContentTypeId().toString() : null)
                            //.teamName(r.getTeam() != null ? r.getTeam().getTeamName() : null)
                            //.createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                            .build();
                })
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
