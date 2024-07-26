package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "Member", description = "Member 관련 API")
@RequestMapping("/api/members")
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;

    @PostMapping("/join")
    @Operation(summary = "회원가입 API", description = "request 파라미터 : 닉네임, 이름, 로그인 아이디(String), 비밀번호(String), 이메일, 생일(yyyymmdd), 성별(MALE, FEMALE, NO_SELECET), 폰번호(010xxxxxxxx),이용약관(Boolean 배열)")
    public ApiResponse<MemberResponseDTO.JoinResultDTO> join(@RequestBody @Valid MemberRequestDTO.JoinDTO request) {
        Member member = memberCommandService.join(request);

        return ApiResponse.onSuccess(MemberConverter.toJoinResultDTO(member));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "request 파라미터 : 이메일, 비밀번호, response : jwt token")
    public ApiResponse<MemberResponseDTO.LoginResultDTO> login(@RequestBody MemberRequestDTO.MemberLoginDTO request){
        String email = request.getEmail();
        String password = request.getPassword();

        String jwt = memberCommandService.login(email,password);

        return ApiResponse.onSuccess(MemberConverter.toLoginResultDTO(jwt));
    }

    @PostMapping("/email/duplicate")
    @Operation(summary = "이메일 중복 체크 API", description = "request : 이메일, response : 중복이면 false, 중복이 아니면 true")
    public ApiResponse<MemberResponseDTO.LoginIdDuplicateConfirmResultDTO> emailDuplicate(@RequestBody MemberRequestDTO.EmailDuplicateConfirmDTO request) {
        Boolean checkLoginId = memberCommandService.confirmEmailDuplicate(request);

        return ApiResponse.onSuccess(MemberConverter.toLoginIdDuplicateConfirmResultDTO(checkLoginId));
    }

    @PostMapping("/nickname/duplicate")
    @Operation(summary = "닉네임 중복 체크 API", description = "request : 닉네임, response: 중복이면 false, 중복 아니면 true")
    public ApiResponse<MemberResponseDTO.NicknameDuplicateConfirmResultDTO> nicknameDuplicate(@RequestBody MemberRequestDTO.NicknameDuplicateConfirmDTO request) {
        Boolean checkNickname = memberCommandService.confirmNicknameDuplicate(request);

        return ApiResponse.onSuccess(MemberConverter.toNicknameDuplicateConfirmResultDTO(checkNickname));
    }

    @PostMapping("/email/auth")
    @Operation(summary = "email 인증 요청 api")
    public ApiResponse<MemberResponseDTO.EmailAuthSendResultDTO> emailAuthSend(@RequestBody MemberRequestDTO.EmailAuthDTO request){
        Auth auth = memberCommandService.sendEmailAuth(request.getEmail());
        return ApiResponse.onSuccess(MemberConverter.toEmailAuthSendResultDTO(auth));
    }

    @PostMapping("/email/auth/verify")
    @Operation(summary = "email 인증 검증 api")
    public ApiResponse<MemberResponseDTO.EmailAuthConfirmResultDTO> emailAuth(@RequestBody MemberRequestDTO.EmailAuthConfirmDTO request){
        String email = request.getEmail();
        Boolean checkEmail = memberCommandService.confirmEmailAuth(request);
        return ApiResponse.onSuccess(MemberConverter.toEmailAuthConfirmResultDTO(email, checkEmail));
    }

    @PostMapping("/jwt/test")
    @Operation(summary = "jwt test API", description = "테스트 용도 api")
    public ResponseEntity<?> jwtTest(Authentication authentication) {
        //request값으로 Bearer {jwt} 값을 넘겨주면 jwt를 해석해서 Authentication에 정보가 담기고 담긴 정보를 가공해서 사용
        //jwt 토큰은 회원가입하고 로그인하면 발급받을 수 있습니다.
        Member member = memberQueryService.findMemberByName((authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return ResponseEntity.ok().body(member.getEmail());
    }

    //소셜 로그인 후 회원가입
    @PostMapping("/complete-signup/{memberId}")
    @Operation(summary = "추가 정보 입력 API", description = "소셜 로그인 후 추가 정보 입력을 처리합니다.")
    public ApiResponse<MemberResponseDTO.CompleteSignupResultDTO> completeSignup(@PathVariable Long memberId, @RequestBody @Valid MemberRequestDTO.CompleteSignupDTO request) {
        Member member = memberCommandService.completeSignup(memberId, request);
        return ApiResponse.onSuccess(MemberConverter.toCompleteSignupResultDTO(member));
    }

    // 아이디 찾기
    @PostMapping("/find/id")
    @Operation(summary = "전화번호로 사용자 찾기", description = "request 파라미터: 전화번호, response: 사용자 정보")
    public ApiResponse<MemberResponseDTO.SearchIdDTO> findUserByPhone(@RequestBody @Valid MemberRequestDTO.SearchIdDTO request) {
        String phone = request.getPhone();
        Optional<Member> member = memberRepository.findByPhone(phone);
        if (member.isPresent()) {
            Member foundmember = member.get();
            return ApiResponse.onSuccess(MemberConverter.toSearchIdResultDTO(foundmember));
        } else {
            return ApiResponse.onFailure("404", "User not found", null);
        }
    }
}