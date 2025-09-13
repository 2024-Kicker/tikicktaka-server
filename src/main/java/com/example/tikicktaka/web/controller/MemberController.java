package com.example.tikicktaka.web.controller;


import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.config.springSecurity.utils.JwtUtil;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.member.Auth;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.infra.kakao.KakaoLoginParams;
import com.example.tikicktaka.repository.member.MemberRepository;
//import com.example.tikicktaka.service.OAuthService.OAuthLoginService;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.web.dto.auth.TokenStatusResponseDTO;
import com.example.tikicktaka.web.dto.auth.TokenResponseDTO;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.env.Environment;

import java.util.*;


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
    private final Environment environment;


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


    @PostMapping("/find/password")
    @Operation(summary = "비밀번호 찾기 api", description = "request : 이메일, 변경할 비밀번호")
    public ApiResponse<MemberResponseDTO.ChangePasswordResultDTO> findPassword(@RequestBody @Valid MemberRequestDTO.ChangePasswordRequestDTO request){
        Member member = memberCommandService.changePassword(request);
        return ApiResponse.onSuccess(MemberConverter.changePasswordResultDTO(member));
    }

    @GetMapping("/auth/check")
    @Operation(summary = "엑세스 토큰 유효성 체크", description = "accessToken 쿼리 파라미터 사용")
    public ApiResponse<TokenStatusResponseDTO> check(
            @RequestParam(value = "accessToken", required = false) String tokenParam) {

        // 1) 시크릿 세팅
        String secret = environment.getProperty("jwt.token.secret");
        JwtUtil.setSecretKeyString(secret);

        // 2) 토큰 추출
        String token = tokenParam;
        // 3) 토큰 유무/만료/타입 검사
        if (token == null || token.isBlank()) {
            return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                    .isLoggedIn(false).memberId(null).role(null)
                    .expEpochSeconds(0L).remainingSeconds(0L)
                    .build());
        }
        try {
            if (JwtUtil.isExpired(token)) {
                return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                        .isLoggedIn(false).memberId(null).role(null)
                        .expEpochSeconds(0L).remainingSeconds(0L)
                        .build());
            }
            // typ이 있으면 access만 인정(과거 토큰은 typ 미포함일 수 있음)
            String typ = null;
            try { typ = JwtUtil.getTokenType(token); } catch (Exception ignore) {}
            if (typ != null && !"access".equals(typ)) {
                return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                        .isLoggedIn(false).memberId(null).role(null)
                        .expEpochSeconds(0L).remainingSeconds(0L)
                        .build());
            }

            Long memberId = JwtUtil.getMemberId(token);

            // ===== ver 비교 (로그아웃 즉시 반영) =====
            Integer tokenVer = 0;
            try { Integer v = JwtUtil.getTokenVersion(token); if (v != null) tokenVer = v; } catch (Exception ignore) {}
            int currentVer = memberQueryService.findMemberById(memberId)
                    .map(Member::getTokenVersion)
                    .orElse(0);
            if (!tokenVer.equals(currentVer)) {
                return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                        .isLoggedIn(false).memberId(null).role(null)
                        .expEpochSeconds(0L).remainingSeconds(0L)
                        .build());
            }
            List<String> roles = JwtUtil.getRole(token);
            String role = (roles == null || roles.isEmpty()) ? null : roles.get(0);

            long exp = 0L, now = System.currentTimeMillis() / 1000;
            try { exp = JwtUtil.getExpEpochSeconds(token); } catch (Exception ignore) {}

            return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                    .isLoggedIn(true)
                    .memberId(memberId)
                    .role(role)
                    .expEpochSeconds(exp)
                    .remainingSeconds(Math.max(0, exp - now))
                    .build());

        } catch (Exception e) {
            return ApiResponse.onSuccess(TokenStatusResponseDTO.builder()
                    .isLoggedIn(false).memberId(null).role(null)
                    .expEpochSeconds(0L).remainingSeconds(0L)
                    .build());
        }
    }


    @PostMapping("/auth/refresh")
    @Operation(
            summary = "리프레시 토큰으로 액세스 토큰 재발급",
            description = "리프레시 토큰으로 만료된 액세스 토큰 재발급"
    )
    public ApiResponse<TokenResponseDTO> refresh(
            @RequestParam("refreshToken") String refreshToken) {

        // 1) 설정 로드
        String secret = environment.getProperty("jwt.token.secret");
        int accessTtlSec  = environment.getProperty("jwt.token.access-token-validity-seconds", Integer.class, 900);
        int refreshTtlSec = environment.getProperty("jwt.token.refresh-token-validity-seconds", Integer.class, 1209600);
        JwtUtil.setSecretKeyString(secret);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiResponse.onFailure("401", "refresh token not provided", null);
        }

        try {
            // 2) RT 기본 검증(만료/타입)
            if (JwtUtil.isExpired(refreshToken)) {
                return ApiResponse.onFailure("401", "refresh token expired", null);
            }
            String typ = null;
            try { typ = JwtUtil.getTokenType(refreshToken); } catch (Exception ignore) {}
            if (typ != null && !"refresh".equals(typ)) {
                return ApiResponse.onFailure("401", "not a refresh token", null);
            }

            // 3) 토큰 버전 검증: RT에 담긴 ver vs DB의 현재 ver
            Long memberId = JwtUtil.getMemberId(refreshToken);
            Member member = memberQueryService.findMemberById(memberId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

            // DB 기준 최신 ver로 재발급
            int currentVer = 0; try { currentVer = member.getTokenVersion(); } catch (Exception ignore) {}

            String memberName = JwtUtil.getMembername(refreshToken);
            List<String> roles = JwtUtil.getRole(refreshToken);

            String newAccess  = JwtUtil.createJwt(memberId, memberName, accessTtlSec * 1000, secret, roles, currentVer);
            String newRefresh = JwtUtil.createRefreshJwt(memberId, memberName, refreshTtlSec * 1000, secret, roles, currentVer);

            long accessExp = 0L, refreshExp = 0L;
            try { accessExp = JwtUtil.getExpEpochSeconds(newAccess); } catch (Exception ignore) {}
            try { refreshExp = JwtUtil.getExpEpochSeconds(newRefresh); } catch (Exception ignore) {}

            TokenResponseDTO body = TokenResponseDTO.builder()
                    .tokenType("Bearer")
                    .accessToken(newAccess)
                    .accessTokenExpiresIn(accessExp)
                    .refreshToken(newRefresh)
                    .refreshTokenExpiresIn(refreshExp)
                    .build();

            return ApiResponse.onSuccess(body);
        } catch (Exception e) {
            return ApiResponse.onFailure("401", "invalid refresh token", null);
        }
    }


    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    public ApiResponse<Void> logout(
            @RequestParam(value = "token", required = false) String tokenParam) {

        String secret = environment.getProperty("jwt.token.secret");
        JwtUtil.setSecretKeyString(secret);

        String token = null;
        if (token == null) token = tokenParam;

        if (token == null || token.isBlank()) {
            return ApiResponse.onSuccess(null); // idempotent
        }

        try {
            // (선택) access 토큰인지 확인
            String typ = null; try { typ = JwtUtil.getTokenType(token); } catch (Exception ignore) {}
            if (typ != null && !"access".equals(typ)) {
                return ApiResponse.onSuccess(null);
            }

            Long memberId = JwtUtil.getMemberId(token);
            Member m = memberRepository.findById(memberId)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
            m.increaseTokenVersion();              // ★ 버전 +1
            memberRepository.save(m);
        } catch (Exception e) {
            log.debug("logout error: {}", e.getMessage());
            // 실패해도 응답은 성공(클라이언트는 토큰 삭제)
        }
        return ApiResponse.onSuccess(null);
    }

}
