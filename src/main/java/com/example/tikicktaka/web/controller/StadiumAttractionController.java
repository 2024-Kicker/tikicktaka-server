//package com.example.tikicktaka.web.controller;
//
//import com.example.tikicktaka.apiPayload.ApiResponse;
//import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
//import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
//import com.example.tikicktaka.converter.stadiumAttraction.StadiumAttractionConverter;
//import com.example.tikicktaka.domain.mapping.member.MemberTeam;
//import com.example.tikicktaka.domain.member.Member;
//import com.example.tikicktaka.domain.travel.TravelLocation;
//import com.example.tikicktaka.service.memberService.MemberQueryService;
//import com.example.tikicktaka.repository.member.MemberTeamRepository;
//import com.example.tikicktaka.service.stadiumAttractionService.StadiumAttractionQueryService;
//import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springdoc.core.annotations.ParameterObject;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/stadium-attractions")
//@RequiredArgsConstructor
//@Tag(name = "StadiumAttraction", description = "구장별 관광지 API")
//public class StadiumAttractionController {
//
//    private final MemberQueryService memberQueryService;
//    private final MemberTeamRepository memberTeamRepository;
//    private final StadiumAttractionQueryService stadiumAttractionQueryService;
//
//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    @Operation(summary = "구장별 관광지 목록 조회", description = "teamId가 없으면 로그인 사용자의 선호 팀으로 기본 조회합니다.")
//    public ApiResponse<Page<StadiumAttractionResponseDTO.Item>> getStadiumAttractions(
//            Authentication authentication,
//            @RequestParam(required = false) Long teamId,
//            @RequestParam(required = false) Double lat,
//            @RequestParam(required = false) Double lng,
//            @RequestParam(required = false, defaultValue = "popularity") String sort,   // popularity | rating | distance
//            @RequestParam(required = false) String category,                            // 예: "food,cafe"
//            @PageableDefault(page = 0, size = 20) @ParameterObject Pageable pageable
//    ) {
//        Long effectiveTeamId = teamId;
//
//        // teamId 미전달 시 → 로그인 유저의 선호 팀으로 대체
//        if (effectiveTeamId == null) {
//            if (authentication == null || authentication.getName() == null) {
//                return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
//                        ErrorStatus._UNAUTHORIZED.getMessage(), null);
//            }
//
//            Long memberId = Long.valueOf(authentication.getName());
//            Member member = memberQueryService.findMemberById(memberId)
//                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
//
//            Optional<MemberTeam> memberTeamOpt = memberTeamRepository.findByMember(member);
//            if (memberTeamOpt.isPresent() && memberTeamOpt.get().getTeam() != null) {
//                effectiveTeamId = memberTeamOpt.get().getTeam().getId();
//            } else {
//                return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(),
//                        "선호 팀이 설정되어 있지 않습니다. teamId를 전달하거나 마이페이지에서 선호 팀을 설정하세요.", null);
//            }
//        }
//
//        // Service: 엔티티 Page로 조회
//        Page<TravelLocation> locationPage = stadiumAttractionQueryService.findByTeam(
//                effectiveTeamId, lat, lng, sort, category, pageable
//        );
//
//        // Converter: 엔티티 Page → DTO Page
//        Page<StadiumAttractionResponseDTO.Item> page = locationPage.map(
//                loc -> StadiumAttractionConverter.toItemDTO(loc, lat, lng, effectiveTeamId, /*stadiumName*/ null)
//        );
//
//        return ApiResponse.onSuccess(page);
//    }
//}

// src/main/java/com/example/tikicktaka/web/controller/StadiumAttractionController.java
package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.domain.enums.TargetType;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.service.blocked.BlockedService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.repository.member.MemberTeamRepository;
import com.example.tikicktaka.service.stadiumAttractionService.StadiumAttractionQueryService;
import com.example.tikicktaka.web.dto.stadiumAttraction.StadiumAttractionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stadium-attractions")
@RequiredArgsConstructor
@Tag(name = "StadiumAttraction", description = "구장별 관광지 API")
public class StadiumAttractionController {

    private final MemberQueryService memberQueryService;
    private final MemberTeamRepository memberTeamRepository;
    private final StadiumAttractionQueryService stadiumAttractionQueryService;
    private final BlockedService blockedService;

    /**
     * 기본: 팀 기준(내 선호팀 or teamId)으로 필터 없이 조회
     * 옵션: myScrapOnly=true / categoryCodes=A01,A05 / useDefaultCategory=true(마이페이지 저장 키워드 사용, 최대 2개)
     */
//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ApiResponse<Page<StadiumAttractionResponseDTO.Item>> getStadiumAttractions(
//            Authentication authentication,
//            @RequestParam(required = false) Long teamId,
//            @RequestParam(required = false) String categoryCodes,                 // 예: "A01,A05"
//            @RequestParam(defaultValue = "false") boolean useDefaultCategory,    // 기본: 필터 미적용
//            @RequestParam(defaultValue = "false") boolean myScrapOnly,           // 기본: 필터 미적용
//            @RequestParam(defaultValue = "popularity") String sort,              // popularity | rating | distance(미구현)
//            @PageableDefault(page = 0, size = 20) @ParameterObject Pageable pageable
//    ) {
//        if (authentication == null || authentication.getName() == null) {
//            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
//                    ErrorStatus._UNAUTHORIZED.getMessage(), null);
//        }
//        Long memberId = Long.valueOf(authentication.getName());
//
//        // 1) 팀 결정: 쿼리 > 내 선호팀
//        Long effectiveTeamId = teamId;
//        if (effectiveTeamId == null) {
//            Member member = memberQueryService.findMemberById(memberId)
//                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
//
//            Optional<MemberTeam> memberTeamOpt = memberTeamRepository.findByMember(member);
//            if (memberTeamOpt.isPresent() && memberTeamOpt.get().getTeam() != null) {
//                effectiveTeamId = memberTeamOpt.get().getTeam().getId();
//            } else {
//                return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(),
//                        "선호 팀이 설정되어 있지 않습니다. teamId를 전달하거나 마이페이지에서 선호 팀을 설정하세요.", null);
//            }
//        }
//
//        // 1) 팀 결정: 쿼리 > 내 선호팀
//        List<String> categoryList = StadiumAttractionQueryService.parseCategoryCsv(categoryCodes);
//
//        // ✅ 차단 목록 ID 가져오기 (TargetType은 프로젝트 enum에 맞춰 조정: TRAVEL_LOCATION 또는 TRAVEL_REGION)
//        List<Long> blockedIds = blockedService.blockedIds(memberId, TargetType.TRAVEL_LOCATION);
//
//        Page<StadiumAttractionResponseDTO.Item> page = stadiumAttractionQueryService.findItems(
//                memberId,
//                effectiveTeamId,
//                categoryList,
//                useDefaultCategory,
//                myScrapOnly,
//                sort,
//                pageable,
//                blockedIds
//        );
//
//        return ApiResponse.onSuccess(page);
//    }
    // src/main/java/com/example/tikicktaka/web/controller/StadiumAttractionController.java
    // StadiumAttractionController.java (일부)
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "구장별 관광지 목록 조회(리스트, 차단 제외)")
    public ApiResponse<List<StadiumAttractionResponseDTO.Item>> getStadiumAttractionsAsList(
            Authentication authentication,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String categoryCodes,
            @RequestParam(defaultValue = "false") boolean useDefaultCategory,
            @RequestParam(defaultValue = "false") boolean myScrapOnly,
            @RequestParam(defaultValue = "popularity") String sort
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ApiResponse.onFailure(ErrorStatus._UNAUTHORIZED.getCode(),
                    ErrorStatus._UNAUTHORIZED.getMessage(), null);
        }
        Long memberId = Long.valueOf(authentication.getName());

        // 팀 결정(생략: 너 현재 코드 그대로)
        Long effectiveTeamId = resolveTeamId(authentication, teamId);

        // 카테고리 파싱
        List<String> categoryList = StadiumAttractionQueryService.parseCategoryCsv(categoryCodes);

        // 차단된 TravelRegion ID들
        List<Long> blockedIds = blockedService.blockedIds(memberId, TargetType.TRAVEL_REGION);

        // 4) 서비스는 그대로 사용. 페이징 없이 전부 가져오기
        //   - 가능하면 Pageable.unpaged() 사용
        //   - 만약 내부 구현이 unpaged 미지원이면 PageRequest.of(0, 200) 같은 상한 사용
        Page<StadiumAttractionResponseDTO.Item> page = stadiumAttractionQueryService.findItems(
                memberId,
                effectiveTeamId,
                categoryList,
                useDefaultCategory,
                myScrapOnly,
                sort,
                Pageable.unpaged() // 또는: PageRequest.of(0, 200)
        );

        // 5) 컨트롤러에서 차단 제외
        List<StadiumAttractionResponseDTO.Item> filtered = page.getContent().stream()
                .filter(it -> it.getId() != null && !blockedIds.contains(it.getId()))
                .toList();

        return ApiResponse.onSuccess(filtered);
    }

    // 팀 결정 유틸(네 코드 스타일에 맞춰 이미 있으면 그거 써도 됨)
    private Long resolveTeamId(Authentication authentication, Long teamId) {
        if (teamId != null) return teamId;
        Long memberId = Long.valueOf(authentication.getName());
        Member member = memberQueryService.findMemberById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return memberTeamRepository.findByMember(member)
                .map(mt -> mt.getTeam().getId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._BAD_REQUEST));
    }


}

