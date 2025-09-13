package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.infra.kakao.KakaoLoginParams;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.service.OAuthService.OAuthLoginService;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.service.smsService.SmsService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Random;


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
    private final SmsService smsService;


    @PostMapping(value ="/join", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "회원가입 API", description = "request 파라미터 : 닉네임, 이름, 로그인 아이디(String), 비밀번호(String), 이메일, 성별(MALE, FEMALE, NO_SELECET),소개메시지(String),이용약관(Boolean 배열)")
    public ApiResponse<MemberResponseDTO.JoinResultDTO> join(@Parameter(description = "회원가입 정보 (JSON)", required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MemberRequestDTO.JoinDTO.class)))
                                                                 @RequestPart("data") @Valid MemberRequestDTO.JoinDTO request,  // JSON 데이터 받기

                                                             @Parameter(description = "프로필 이미지 파일 (선택 사항)", required = false)
                                                                 @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        request.setProfileImg(profileImage);
        Member member = memberCommandService.join(request);

        if (profileImage != null && !profileImage.isEmpty()) {
            member = memberCommandService.profileImageUpload(profileImage, member);
        }

        return ApiResponse.onSuccess(MemberConverter.toJoinResultDTO(member));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "request 파라미터 : 이메일, 비밀번호, response : jwt token")
    public ApiResponse<MemberResponseDTO.LoginResultDTO> login(@RequestBody MemberRequestDTO.MemberLoginDTO request,
                                                               HttpServletResponse response) {
        String email = request.getEmail();
        String password = request.getPassword();
        // 1) 서비스에서 Access Token 발급
        String accessToken = memberCommandService.login(email, password);

        // 2) 환경값 + 키 세팅
        String secret = environment.getProperty("jwt.token.secret");
        int refreshTtlSec = environment.getProperty("jwt.token.refresh-token-validity-seconds", Integer.class); // 필요시 기본값 유지/제거
        JwtUtil.setSecretKeyString(secret);

        // 3) AT에서 식별값 추출
        Long memberId = JwtUtil.getMemberId(accessToken);
        String memberName = JwtUtil.getMembername(accessToken);
        List<String> roles = JwtUtil.getRole(accessToken); // 없으면 null 가능

        // 4) (옵션) 즉시 로그아웃용 ver: 멤버 tokenVersion 있으면 반영, 없으면 0
        Member m = memberQueryService.findMemberById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        int ver = m.getTokenVersion();

        // 5) Refresh 토큰 생성 → 헤더로 내려줌(앱 저장용). 실패해도 로그인 자체는 성공.
        String refreshToken = JwtUtil.createRefreshJwt(
                memberId, memberName, refreshTtlSec * 1000, secret, roles, ver /* 없으면 오버로드에서 제거 */);

        long refreshExp = 0L;
        try { refreshExp = JwtUtil.getExpEpochSeconds(refreshToken); } catch (Exception ignore) {}

        // 6) 바디로 모두 반환 (기존 DTO 그대로 사용)
        MemberResponseDTO.LoginResultDTO body =
                MemberConverter.toLoginResultDTO(accessToken, refreshToken);

        return ApiResponse.onSuccess(body);
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

    // SMS 본인인증
    @PostMapping("/sms/auth")
    @Operation(summary = "SMS 인증번호 요청 API", description = "사용자의 휴대폰 번호로 인증번호를 전송합니다.")
    public ApiResponse<MemberResponseDTO.SmsAuthSendResultDTO> sendSmsAuthCode(@RequestBody @Valid MemberRequestDTO.SmsAuthDTO request) {
        Auth auth = memberCommandService.sendSmsAuthCode(request.getPhoneNumber());
        return ApiResponse.onSuccess(MemberConverter.toSmsAuthSendResultDTO(auth));
    }

    @PostMapping("/sms/auth/verify")
    @Operation(summary = "SMS 인증번호 검증 API", description = "사용자가 입력한 인증번호가 올바른지 확인합니다.")
    public ApiResponse<MemberResponseDTO.SmsAuthConfirmResultDTO> verifySmsCode(@RequestBody @Valid MemberRequestDTO.SmsAuthConfirmDTO request) {
        String phoneNumber = request.getPhoneNumber();
        Boolean checkPhone = memberCommandService.confirmSmsAuth(request);
        return ApiResponse.onSuccess(MemberConverter.toSmsAuthConfirmResultDTO(phoneNumber, checkPhone));
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
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return ResponseEntity.ok().body(member.getEmail());
    }

//    private final OAuthLoginService oAuthLoginService;
//    @PostMapping("/kakao")
//    @Operation(summary = "카카오 소셜 로그인 API", description = "카카오 소셜 로그인 response : authorization Code") //이거 실행 부분 수정하기
//    public ResponseEntity<MemberResponseDTO.MemberLoginResponseDTO> loginKakao(@RequestBody KakaoLoginParams params) {
//        MemberResponseDTO.MemberLoginResponseDTO response = oAuthLoginService.login(params);
//        return ResponseEntity.ok(response);
//    }

//    //소셜 로그인 후 회원가입
//    @PostMapping("/complete-signup/{memberId}")
//    @Operation(summary = "추가 정보 입력 API", description = "소셜 로그인 후 추가 정보 입력을 처리합니다.")
//    public ApiResponse<MemberResponseDTO.CompleteSignupResultDTO> completeSignup(@PathVariable Long memberId, @RequestBody @Valid MemberRequestDTO.CompleteSignupDTO request) {
//        Member member = memberCommandService.completeSignup(memberId, request);
//        return ApiResponse.onSuccess(MemberConverter.toCompleteSignupResultDTO(member));
//    }

    // 아이디 찾기
//    @PostMapping("/find/id")
//    @Operation(summary = "전화번호로 사용자 찾기", description = "request 파라미터: 전화번호, response: 사용자 정보")
//    public ApiResponse<MemberResponseDTO.SearchIdDTO> findUserByPhone(@RequestBody @Valid MemberRequestDTO.SearchIdDTO request) {
//        String phone = request.getPhone();
//        Optional<Member> member = memberRepository.findByPhone(phone);
//        if (member.isPresent()) {
//            Member foundmember = member.get();
//            return ApiResponse.onSuccess(MemberConverter.toSearchIdResultDTO(foundmember));
//        } else {
//            return ApiResponse.onFailure("404", "User not found", null);
//        }
//    }

    @PostMapping("/find/password")
    @Operation(summary = "비밀번호 찾기 api", description = "request : 이메일, 변경할 비밀번호")
    public ApiResponse<MemberResponseDTO.ChangePasswordResultDTO> findPassword(@RequestBody @Valid MemberRequestDTO.ChangePasswordRequestDTO request){
        Member member = memberCommandService.changePassword(request);
        return ApiResponse.onSuccess(MemberConverter.changePasswordResultDTO(member));
    }



}
