package com.example.tikicktaka.config.springSecurity.utils;

import com.example.tikicktaka.service.memberService.MemberCommandService;
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

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final MemberCommandService memberCommandService;
    private final String secretKey;

    //Token이 유요한지와 Token이 있는지 없는지 체크하는 로직 구현해야함


    //일단 모든 기능 막아놓고 doFilterInternal을 통해서 기능 인가 허용한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {


        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);//토큰 꺼내기

        //token을 안 넣었을때 막기
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        //토큰 저장
        String token = authorization.split(" ")[1];


        //토큰 expire 여부
        if (JwtUtil.isExpired(token)) {
            log.error("토큰 기간 만료");
            filterChain.doFilter(request, response);
            return;
        }

        //jwt 토큰에서 username 꺼내기
        String memberName = JwtUtil.getMembername(token);

        Long memberId = JwtUtil.getMemberId(token);

        //권한 부여
        //권한이 USER면 통과 가능
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // Detail을 넘겨준다.
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
