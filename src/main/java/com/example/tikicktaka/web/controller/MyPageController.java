package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@io.swagger.v3.oas.annotations.tags.Tag(name = "MyPage", description = "MyPage 관련 API")
@RequestMapping("/api/members/mypage")
public class MyPageController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PutMapping(value = "/profile/modify", consumes = "multipart/form-data")
    @Operation(summary = "마이페이지 프로필 수정(닉네임, 프로필 사진) api", description = "request : 프로필 이미지, 닉네임")
    public ApiResponse<MemberResponseDTO.ProfileModifyResultDTO> profileModify(@RequestParam("profile") MultipartFile profile,
                                                                               @RequestParam("nickname") String nickname,
                                                                               Authentication authentication) {
        Member member = memberQueryService.findMemberByName((authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));


        Member modifyMember = memberCommandService.profileModify(profile, nickname, member);

        return ApiResponse.onSuccess(MemberConverter.toProfileModify(modifyMember));
    }

    @PostMapping(value = "/teams/{teamId}")
    @Operation(summary = "선호 구단 등록 API", description = "request : 팀 id.")
    public ApiResponse<MemberResponseDTO.MemberPreferTeamDTO> preferTeam(@RequestParam("teamId") Long teamId,
                                                                         Authentication authentication){
        Member member = memberQueryService.findMemberByName((authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        MemberTeam memberTeam = memberCommandService.setPreferTeam(member, teamId);
        return ApiResponse.onSuccess(MemberConverter.toMemberPreferTeamDTO(memberTeam));
    }

}
