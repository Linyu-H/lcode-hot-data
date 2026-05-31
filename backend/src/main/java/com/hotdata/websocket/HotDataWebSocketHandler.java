package com.hotdata.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotSnapshot;
import com.hotdata.service.HotDataService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class HotDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HotDataWebSocketHandler.class);

    private final HotDataService service;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public HotDataWebSocketHandler(HotDataService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        service.addListener(this::broadcast);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        HotSnapshot latest = service.latest();
        if (latest != null) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(latest)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private void broadcast(HotSnapshot snapshot) {
        String payload;
        try {
            payload = mapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.warn("serialize snapshot failed: {}", e.toString());
            return;
        }
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    synchronized (s) {
                        s.sendMessage(new TextMessage(payload));
                    }
                }
            } catch (IOException e) {
                log.debug("send failed, dropping session: {}", e.toString());
                sessions.remove(s);
            }
        }
    }
}
