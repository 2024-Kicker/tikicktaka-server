package com.example.tikicktaka.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Map;

@Configuration
public class SocketIOConfig {

    private final String host;
    private final Integer port;
    private final SocketIOServer server;
    private final Logger logger = LoggerFactory.getLogger(SocketIOConfig.class);


    // 생성자에서 @Value 값 주입
    public SocketIOConfig(@Value("${socketio.host}") String host,
                          @Value("${socketio.port}") Integer port) {
        this.host = host;
        this.port = port;

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setOrigin("*");
        config.setPort(port);
        config.setAllowCustomRequests(true);
        config.setUpgradeTimeout(10000);
        config.setPingInterval(25000);
        config.setPingTimeout(60000);
        config.setMaxFramePayloadLength(1048576);
        config.setMaxHttpContentLength(1048576);
        config.setOrigin("*");

        this.server = new SocketIOServer(config);
    }

    @Bean
    public SocketIOServer socketIOServer() {
        logger.info("Initializing Socket.IO server...");


        // 클라이언트 연결 이벤트 리스너 추가
        server.addConnectListener(client -> {
            System.out.println("1: Client Connected: " + client.getSessionId());        });

        // 클라이언트 연결 종료 이벤트 리스너 추가
        server.addDisconnectListener(client -> {
            System.out.println("1: Client Disconnected: " + client.getSessionId());
        });

        logger.info("1: Socket.IO 서버 설정 완료: ws://{}:{}", host, port);


        return server;  // this.server가 아니라 server를 반환
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startServer() {
        logger.info("Socket.IO 서버 실행: ws://{}:{}", host, port);
        server.start();
    }


    @PreDestroy
    public void stopServer() {
        if (server != null) {
            logger.info("Socket.IO 서버 종료");
            server.stop();
        }
    }


}
