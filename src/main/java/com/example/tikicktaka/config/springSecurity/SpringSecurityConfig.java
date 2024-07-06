package com.example.tikicktaka.config.springSecurity;

import com.example.tikicktaka.config.springSecurity.utils.JwtTokenFilter;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final MemberCommandService memberCommandService;

    @Value("${JWT_TOKEN_SECRET}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)//토큰 인증 방식으로 하기 위해서 HTTP 기본 인증 비활성화
                .csrf(AbstractHttpConfigurer::disable)//CSRF 공격 방어 기능 비활성화
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtTokenFilter(memberCommandService, secretKey), UsernamePasswordAuthenticationFilter.class)
                //UsernamePasswordAuthenticationFilter 앞에 JwtTokenFilter를 둔다. 그 이유는 이미 로그인을 했고 토큰을 발급 받아서 토큰 이용해서 인증하면 된다.
                .build();
    }
}
