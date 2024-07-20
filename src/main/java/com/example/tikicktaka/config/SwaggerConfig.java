package com.example.tikicktaka.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;  // 추가된 import 문

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tikicktakaAPI() {
        Info info = new Info()
                .title("TikickTaka Server API")
                .description("TikickTaka Server API 명세서")
                .version("1.0.0");

        String oauth2SchemeName = "oauth2";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(oauth2SchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(oauth2SchemeName, new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2) // OAuth2 방식
                        .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                                .tokenUrl("https://oauth2.googleapis.com/token")
                                .authorizationUrl("https://accounts.google.com/o/oauth2/auth")
                                .scopes(new Scopes().addString("profile", "프로필 정보에 대한 접근 권한")
                                        .addString("email", "이메일 주소에 대한 접근 권한")))));

        return new OpenAPI()
                .addServersItem(new Server().url("https://api.example.com")) // 실제 서버 URL로 수정
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

}
