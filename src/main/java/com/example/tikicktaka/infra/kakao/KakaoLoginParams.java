package com.example.tikicktaka.infra.kakao;
import com.example.tikicktaka.domain.oauth.OAuthLoginParams;
import com.example.tikicktaka.domain.oauth.oauthprovider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
@NoArgsConstructor
public class KakaoLoginParams implements OAuthLoginParams {
    private String authorizationCode;

    public oauthprovider oauthprovider(){
        return oauthprovider.KAKAO;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code",authorizationCode);
        return body;
    }
}
