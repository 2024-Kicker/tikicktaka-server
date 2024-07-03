package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "Member", description = "Member 관련 API")
@RequestMapping("/api/members")
public class MemberController {

    private final MemberCommandService memberCommandService;

    @PostMapping("/join")
    @Operation(summary = "회원가입 API", description = "request 파라미터 : 닉네임, 이름, 로그인 아이디(String), 비밀번호(String), 이메일, 생일(yyyymmdd), 성별(MALE, FEMALE, NO_SELECET), 폰번호(010xxxxxxxx),이용약관(Boolean 배열)")
    public ApiResponse<MemberResponseDTO.JoinResultDTO> join(@RequestBody @Valid MemberRequestDTO.JoinDTO request) {
        Member member = memberCommandService.join(request);

        return ApiResponse.onSuccess(MemberConverter.toJoinResultDTO(member));
    }

    @PostMapping("/email/duplicate")
    @Operation(summary = "이메일 중복 체크 API", description = "request : 이메일, response : 중복이면 false, 중복이 아니면 true")
    public ApiResponse<MemberResponseDTO.LoginIdDuplicateConfirmResultDTO> emailDuplicate(@RequestBody MemberRequestDTO.LoginIdDuplicateConfirmDTO request) {
        Boolean checkLoginId = memberCommandService.confirmLoginIdDuplicate(request);

        return ApiResponse.onSuccess(MemberConverter.toLoginIdDuplicateConfirmResultDTO(checkLoginId));
    }

    @PostMapping("/nickname/duplicate")
    @Operation(summary = "닉네임 중복 체크 API", description = "request : 닉네임, response: 중복이면 false, 중복 아니면 true")
    public ApiResponse<MemberResponseDTO.NicknameDuplicateConfirmResultDTO> nicknameDuplicate(@RequestBody MemberRequestDTO.NicknameDuplicateConfirmDTO request) {
        Boolean checkNickname = memberCommandService.confirmNicknameDuplicate(request);

        return ApiResponse.onSuccess(MemberConverter.toNicknameDuplicateConfirmResultDTO(checkNickname));
    }
}
