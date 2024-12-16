package com.example.api.configuration;

import com.example.api.communication.WebSocketHandler;
import com.example.api.data.access.repositories.PlayerRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final PlayerRepository playerRepository;

    public WebSocketConfig(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(playerRepository), "/websocket").setAllowedOrigins("*");
    }
}
