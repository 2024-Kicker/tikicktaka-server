package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.infra.kakao.KakaoLoginParams;
import com.example.tikicktaka.service.OAuthService.OAuthLoginService;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final OAuthLoginService oAuthLoginService;
    @PostMapping("/kakao")
    public ResponseEntity<MemberResponseDTO.MemberLoginResponseDTO> loginKakao(@RequestBody KakaoLoginParams params) {
        MemberResponseDTO.MemberLoginResponseDTO response = oAuthLoginService.login(params);
        return ResponseEntity.ok(response);
    }
}