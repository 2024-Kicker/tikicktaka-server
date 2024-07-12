package com.example.tikicktaka.domain.oauth;
import org.springframework.util.MultiValueMap;

public interface OAuthLoginParams {
    oauthprovider oauthprovider();
    MultiValueMap<String,String> makeBody();
}
