package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.service.memberService.CustomOAuth2UserService;
import com.example.tikicktaka.service.memberService.JwtTokenProvider;
import com.example.tikicktaka.domain.member.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/oauth2")
public class LoginController {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    public LoginController(JwtTokenProvider jwtTokenProvider, CustomOAuth2UserService customOAuth2UserService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @GetMapping("/google")
    public String loginUrlGoogle() {
        String googleClientId = "716853268200-a1dmd391fjb6gq2ge54n966hkf3v7j92.apps.googleusercontent.com";
        String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId
                + "&redirect_uri=http://localhost:8080/api/v1/oauth2/google&response_type=code&scope=email%20profile%20openid&access_type=offline";
        return reqUrl;
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess() {
        User currentUser = customOAuth2UserService.getCurrentUser();
        String token = jwtTokenProvider.createToken(currentUser);
        return "JWT Token: " + token;
    }
}
