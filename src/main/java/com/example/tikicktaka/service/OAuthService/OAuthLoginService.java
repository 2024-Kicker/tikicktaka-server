package com.example.tikicktaka.service.OAuthService;

import com.example.tikicktaka.config.springSecurity.utils.JwtUtil;
import com.example.tikicktaka.domain.oauth.OAuthInfoResponse;
import com.example.tikicktaka.domain.oauth.RequestOAuthInfoService;
import com.example.tikicktaka.infra.kakao.KakaoLoginParams;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.enums.MemberRole;
import com.example.tikicktaka.repository.member.MemberRepository;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberRepository memberRepository;
    private final RequestOAuthInfoService requestOAuthInfoService;
    private final JwtUtil jwtUtil; // JWT 유틸리티 추가
    @Value("${JWT_TOKEN_SECRET}")
    private String key;

    private int expiredMs = 1000 * 60 * 60 * 24 * 5;

    public MemberResponseDTO.MemberLoginResponseDTO login(KakaoLoginParams params) {
        OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
        Long memberId = findOrCreateMember(oAuthInfoResponse);

        Member member = memberRepository.findByEmail(oAuthInfoResponse.getEmail()).orElseThrow(()-> new IllegalArgumentException("Member not found"));

        String jwt = jwtUtil.createJwt(member.getId(), member.getNickname(), expiredMs, key, List.of("MEMBER"));
        return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getEmail(), member.getNickname(),jwt);
    }

    private Long findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
        return memberRepository.findByEmail(oAuthInfoResponse.getEmail())
                .map(Member::getId)
                .orElseGet(() -> newMember(oAuthInfoResponse));
    }

    private Long newMember(OAuthInfoResponse oAuthInfoResponse) {
        Member member = Member.builder()
                .email(oAuthInfoResponse.getEmail())
                .nickname(oAuthInfoResponse.getNickname())
                .socialType(oAuthInfoResponse.getSocialType())
                .memberRole(MemberRole.MEMBER) // 기본 롤 설정
                .build();

        return memberRepository.save(member).getId();
    }
}
