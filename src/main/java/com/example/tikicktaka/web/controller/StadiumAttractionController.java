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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/stadium-attractions")
@RequiredArgsConstructor
@Tag(name = "StadiumAttraction", description = "구장별 관광지 API")
public class StadiumAttractionController {

    private final MemberQueryService memberQueryService;
    private final MemberTeamRepository memberTeamRepository;
    private final StadiumAttractionQueryService stadiumAttractionQueryService;
    private final BlockedService blockedService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "구장별 관광지 목록(단일 엔드포인트)",
            description = "`unpaged=false`(기본) → Page 반환, `unpaged=true` → List 반환.\n" +
                    "teamId/styleIds 미전달 시 로그인 사용자의 선호값 적용, 정렬은 내부 고정."
    )
    public ApiResponse<?> getStadiumAttractions(
            Authentication authentication,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String styleIds,  // "1,5"
            @RequestParam(defaultValue = "false") boolean myScrapOnly,
            @RequestParam(defaultValue = "false") boolean unpaged,
            @RequestParam(required = false, defaultValue = "200") int limit,
            @PageableDefault(page = 0, size = 20) @ParameterObject Pageable pageable
    ) {
        Long memberId = requireLogin(authentication);
        Long effectiveTeamId = resolveTeamId(authentication, teamId);
        List<Long> styleIdList = parseCsvToLongs(styleIds);

        Page<StadiumAttractionResponseDTO.Item> page =
                stadiumAttractionQueryService.findItems(
                        memberId,
                        effectiveTeamId,
                        styleIdList,   // 서비스 시그니처가 List<Long> 기준
                        false,         // useDefaultCategory (사용 안함)
                        myScrapOnly,
                        null,          // sort 고정
                        unpaged ? Pageable.unpaged() : pageable
                );

        // 차단 제외
        List<Long> blockedIds = blockedService.blockedIds(memberId, TargetType.TRAVEL_REGION);
        List<StadiumAttractionResponseDTO.Item> filtered = page.getContent().stream()
                .filter(it -> it.getId() != null && !blockedIds.contains(it.getId()))
                .toList();

        if (unpaged) {
            if (limit > 0 && filtered.size() > limit) filtered = filtered.subList(0, limit);
            return ApiResponse.onSuccess(filtered); // List 반환
        }
        // Page 반환(총개수는 원본 total 유지)
        return ApiResponse.onSuccess(new PageImpl<>(filtered, page.getPageable(), page.getTotalElements()));
    }

    private Long requireLogin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new MemberHandler(ErrorStatus._UNAUTHORIZED);
        }
        return Long.valueOf(authentication.getName());
    }
    private Long resolveTeamId(Authentication authentication, Long teamId) {
        if (teamId != null) return teamId;
        Long memberId = Long.valueOf(authentication.getName());
        Member member = memberQueryService.findMemberById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return memberTeamRepository.findByMember(member)
                .map(MemberTeam::getTeam)
                .map(t -> t.getId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._BAD_REQUEST));
    }
    private List<Long> parseCsvToLongs(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Long> out = new ArrayList<>();
        for (String p : csv.split(",")) {
            try {
                String t = p.trim();
                if (!t.isEmpty()) out.add(Long.valueOf(t));
            } catch (NumberFormatException ignore) {}
        }
        return out;
    }
}
