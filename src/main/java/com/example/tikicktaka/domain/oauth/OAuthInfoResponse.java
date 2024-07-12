package com.example.tikicktaka.domain.oauth;

public interface OAuthInfoResponse {
    String getEmail();
    String getNickname();
    oauthprovider getOAuthProvider();
}
