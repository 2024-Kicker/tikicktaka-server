package com.example.tikicktaka.domain.oauth;
import com.example.tikicktaka.domain.enums.SocialType;
import org.springframework.util.MultiValueMap;

public interface OAuthLoginParams {
    SocialType socialType();
    MultiValueMap<String,String> makeBody();
}