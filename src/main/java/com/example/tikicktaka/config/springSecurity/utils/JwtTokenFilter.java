package com.example.tikicktaka.config.springSecurity.utils;

import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final MemberQueryService memberQueryService; // ★ 추가
    private final String secretKey;



    //일단 모든 기능 막아놓고 doFilterInternal을 통해서 기능 인가 허용한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {

        JwtUtil.setSecretKeyString(secretKey);

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);//토큰 꺼내기

        //token을 안 넣었을때 막기
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //토큰 저장
        //String token = authorization.split(" ")[1];
        String token = authorization.substring(7);
        try {
            // 1) 만료 체크
            if (JwtUtil.isExpired(token)) {
                SecurityContextHolder.clearContext();            // ★ 무효면 컨텍스트 비우기
                filterChain.doFilter(request, response);
                return;
            }

            // 2) access 토큰 타입만 허용
            String typ = null;
            try { typ = JwtUtil.getTokenType(token); } catch (Exception ignore) {}
            if (typ != null && !"access".equals(typ)) {
                SecurityContextHolder.clearContext();            // ★
                filterChain.doFilter(request, response);
                return;
            }

            // 3) ver 비교 (핵심)
            Long memberId = JwtUtil.getMemberId(token);
            Integer tokenVer = 0;
            try {
                Integer v = JwtUtil.getTokenVersion(token);
                if (v != null) tokenVer = v;
            } catch (Exception ignore) {}
            int currentVer = memberQueryService.findMemberById(memberId)
                    .map(Member::getTokenVersion)
                    .orElse(0);
            if (!tokenVer.equals(currentVer)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 4) 권한 세팅
            List<String> roles = JwtUtil.getRole(token); // ← JwtUtil이 "roles" 클레임을 읽도록 보정(아래 참고)
            var authorities = (roles == null || roles.isEmpty())
                    ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    : roles.stream().map(SimpleGrantedAuthority::new).collect(java.util.stream.Collectors.toList());

            var authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberId, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (Exception e) {
            // 파싱 실패 등 모든 예외 시 인증 제거
            SecurityContextHolder.clearContext();                // ★
        }

        filterChain.doFilter(request, response);
    }
}
