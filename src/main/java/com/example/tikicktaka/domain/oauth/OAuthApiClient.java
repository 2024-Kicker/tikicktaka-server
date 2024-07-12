package com.example.tikicktaka.domain.oauth;

public interface OAuthApiClient {
    oauthprovider oauthprovider();
    String requestAccessToken(OAuthLoginParams params);
    OAuthInfoResponse requestOauthInfo(String accessToken);
}
