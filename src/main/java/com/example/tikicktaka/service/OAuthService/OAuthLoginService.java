package com.example.tikicktaka.service.OAuthService;

import com.example.tikicktaka.config.springSecurity.utils.JwtUtil;
import com.example.tikicktaka.domain.enums.Gender;
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
import java.util.Random;


@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberRepository memberRepository;
    private final RequestOAuthInfoService requestOAuthInfoService;
    private final JwtUtil jwtUtil; // JWT 유틸리티 추가
    @Value("${jwt.token.secret}")
    private String key;

    private int expiredMs = 1000 * 60 * 60 * 24 * 5;

    public MemberResponseDTO.MemberLoginResponseDTO login(KakaoLoginParams params) {
        OAuthInfoResponse oAuthInfoResponse = requestOAuthInfoService.request(params);
        //카카오 이메일 DB에 저장되어있지 않으면 생성
        Long memberId = findOrCreateMember(oAuthInfoResponse);

        //저장이 제대로 안되었을 시에 오류남
        Member member = memberRepository.findByEmail(oAuthInfoResponse.getEmail()).orElseThrow(()-> new IllegalArgumentException("Member not found"));

        //추가 회원 정보 기입 여부 받아옴
        boolean hasAdditionalInfo = member.getName() != null &&
                member.getBirthday() != null && member.getPhone() != null;

        if(!hasAdditionalInfo){
            //추가 정보가 없는 경우
            //return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getName(), member.getNickname(), member.getEmail(), null,"추가 정보를 먼저 기입하시길 바랍니다");
            return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getName(), member.getEmail(), null,"추가 정보를 먼저 기입하시길 바랍니다");
        }

        //추가 정보 기입까지 완료된 경우
        String jwt = jwtUtil.createJwt(memberId, member.getName(), expiredMs, key, List.of("MEMBER"));
        //return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getName(), member.getNickname(), member.getEmail(), jwt, "카카오 소셜로그인 완료되었습니다");
        return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getName(), member.getEmail(), jwt, "카카오 소셜로그인 완료되었습니다");
        //return new MemberResponseDTO.MemberLoginResponseDTO(memberId, member.getNickname(), member.getEmail(),jwt, "");
    }

    private Long findOrCreateMember(OAuthInfoResponse oAuthInfoResponse) {
        return memberRepository.findByEmail(oAuthInfoResponse.getEmail())
                .map(Member::getId)
                .orElseGet(() -> newMember(oAuthInfoResponse));
    }

    private Long newMember(OAuthInfoResponse oAuthInfoResponse) {
        String randomNickname = generateRandomNickname();
        Member member = Member.builder()
                .email(oAuthInfoResponse.getEmail())
                //.name(oAuthInfoResponse.getName())
                .name(randomNickname)
                .socialType(oAuthInfoResponse.getSocialType())
                .gender(Gender.NO_SELECT) //성별 기본 설정
                .memberRole(MemberRole.MEMBER) // 기본 롤 설정
                .build();

        return memberRepository.save(member).getId();
    }

    private String generateRandomNickname() {
        Random random = new Random();
        String nickname;
        do {
            int randomNumber = random.nextInt(10000); // 0에서 9999 사이의 랜덤 숫자 생성
            nickname = "이닝" + String.format("%04d", randomNumber);
        } while (memberRepository.existsByName(nickname));
        return nickname;
    }
}
