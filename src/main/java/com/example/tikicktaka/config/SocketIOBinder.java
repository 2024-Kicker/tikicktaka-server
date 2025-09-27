package com.example.tikicktaka.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.example.tikicktaka.service.chatService.ChatEventListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketIOBinder {
    private final SocketIOServer server;
    private final ChatEventListener listener;

    @PostConstruct
    public void bind() {
        server.addListeners(listener);
        log.info("SocketIO annotation listeners bound.");
    }
}
