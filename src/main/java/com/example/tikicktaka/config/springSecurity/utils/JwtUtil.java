package com.example.tikicktaka.config.springSecurity.utils;

import com.example.tikicktaka.config.springSecurity.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Component
public class JwtUtil {

    private static SecretKey secretKey;

    public static String getMembername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberName", String.class);
    }

    public static Long getMemberId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRole(String token) {
        Object raw = claims(token).get("roles");
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) out.add(String.valueOf(o));
            return out;
        }
        return null;
    }

    public static boolean isExpired(String token) {
        Date exp = claims(token).getExpiration();
        return exp.before(new Date());
    }
//파서가 쓸 키를 외부에서 세팅 (필터/컨트롤러에서 1줄 호출)

    public static void setSecretKeyString(String key) {
        secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS512.key().build().getAlgorithm()
        );
    }

    private static Claims claims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }

    //토큰 타입(access/refresh). 과거 토큰엔 없을 수 있음 → null
    public static String getTokenType(String token) {
        return io.jsonwebtoken.Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().get("typ", String.class);
    }

    // 만료 epoch
    public static long getExpEpochSeconds(String token) {
        java.util.Date exp = io.jsonwebtoken.Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getExpiration();
        return exp.getTime() / 1000;
    }

    public static Integer getTokenVersion(String token) {
        Number n = claims(token).get("ver", Number.class);
        return (n == null) ? 0 : n.intValue(); // 하위호환: 없으면 0
    }

    // 액세스 토큰 생성 + typ=access 부여
    public static String createJwt(Long memberId, String memberName, int expiredMs, String key, List<String> roles, int ver) {
        setSecretKeyString(key);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .signWith(secretKey)
                .header().add("type", "JWT").and()
                .claim("memberId", memberId)
                .claim("memberName", memberName)
                .claim("roles", roles)
                .claim("typ", "access")
                .claim("ver", ver)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiredMs))
                .compact();
    }

    // 리프레시 토큰 생성
    public static String createRefreshJwt(Long memberId, String memberName, int expiredMs, String key, List<String> roles, int ver) {
        setSecretKeyString(key);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .signWith(secretKey)
                .header().add("type", "JWT").and()
                .claim("memberId", memberId)
                .claim("memberName", memberName)
                .claim("roles", roles)
                .claim("typ", "refresh")
                .claim("ver", ver)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiredMs))
                .compact();
    }

    public static String createJwt(Long memberId, String memberName, int expiredMs, String key, List<String> roles) {
        return createJwt(memberId, memberName, expiredMs, key, roles, 0);
    }
    public static String createRefreshJwt(Long memberId, String memberName, int expiredMs, String key, List<String> roles) {
        return createRefreshJwt(memberId, memberName, expiredMs, key, roles, 0);
    }
}
