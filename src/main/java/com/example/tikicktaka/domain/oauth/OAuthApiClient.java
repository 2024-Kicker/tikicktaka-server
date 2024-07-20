package com.example.tikicktaka.domain.oauth;

import com.example.tikicktaka.domain.enums.SocialType;

public interface OAuthApiClient {
    SocialType socialType();
    String requestAccessToken(OAuthLoginParams params);
    OAuthInfoResponse requestOauthInfo(String accessToken);
}