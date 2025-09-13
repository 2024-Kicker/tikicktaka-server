package com.example.tikicktaka.web.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponseDTO {
    private final String tokenType;     // "Bearer"
    private final String accessToken;
    private final long accessTokenExpiresIn; // epoch seconds or millis (선호에 맞춰)
    private final String refreshToken;  // 쿠키 사용시 null로 내려도 됨
    private final long refreshTokenExpiresIn;
}
