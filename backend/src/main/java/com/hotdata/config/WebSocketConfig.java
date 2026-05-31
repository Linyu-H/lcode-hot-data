package com.hotdata.config;

import com.hotdata.websocket.HotDataWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final HotDataWebSocketHandler handler;

    public WebSocketConfig(HotDataWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 使用 OriginPatterns 支持通配，允许线上域名(如 hot-data.lcode.space)的WebSocket握手
        // 前端经nginx同源代理访问，这里放开Origin校验
        registry.addHandler(handler, "/ws/hotdata")
                .setAllowedOriginPatterns("*");
    }
}
