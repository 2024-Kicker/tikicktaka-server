package com.example.tikicktaka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0"); // 모든 IP에서 접근 가능하도록 설정
        config.setPort(8081); // 웹소켓 서버 포트

        return new SocketIOServer(config);
    }
}
