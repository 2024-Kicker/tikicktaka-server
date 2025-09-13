package com.example.tikicktaka.web.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenStatusResponseDTO {
    private final boolean isLoggedIn;
    private final Long memberId;     // 인증 실패면 null
    private final String role;       // 인증 실패면 null
    private final long expEpochSeconds; // 0 or -1 if invalid
    private final long remainingSeconds; // 0 if invalid
}
