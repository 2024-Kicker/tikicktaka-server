package com.example.tikicktaka.domain.oauth;

import com.example.tikicktaka.domain.enums.SocialType;

public interface OAuthInfoResponse {
    String getEmail();
    String getName();
    SocialType getSocialType();
}