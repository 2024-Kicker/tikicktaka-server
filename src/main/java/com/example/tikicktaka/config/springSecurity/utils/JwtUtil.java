package com.example.tikicktaka.config.springSecurity.utils;

import com.example.tikicktaka.config.springSecurity.constants.SecurityConstants;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
@Component
public class JwtUtil {

    private static SecretKey secretKey;

    public static String getMembername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberName", String.class);
    }

    public static Long getMemberId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberId", Long.class);
    }

    public static List<String> getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", List.class);
    }

    public static boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public static String createJwt(Long memberId, String memberName, int expiredMs, String key, List<String> roles) {

        secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS512.key().build().getAlgorithm());

        //토큰 생성
        return Jwts.builder()//.signWith(시크릿키, 알고리즘)
                .signWith(secretKey)// jwt 알고리즘 방식
                .header()
                .add("type", SecurityConstants.TOKEN_TYPE)
                .and()
                .claim("memberId", memberId)
                .claim("memberName", memberName)
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs)) // token 유효기간 설정
                .compact();


    }
}
