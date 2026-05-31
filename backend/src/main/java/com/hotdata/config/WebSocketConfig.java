package com.hotdata.config;

import com.hotdata.websocket.HotDataWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final HotDataWebSocketHandler handler;

    @Value("${hotdata.cors.allowed-origins}")
    private String allowedOrigins;

    public WebSocketConfig(HotDataWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/hotdata")
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
