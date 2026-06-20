package com.aireader.v2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage WebSocket connections per novel_id for analysis progress broadcasting.
 */
@Slf4j
@Component
public class ConnectionManager {

    private final Map<String, List<WebSocketSession>> connections = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ConnectionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Connect a WebSocket session to a novel.
     */
    public void connect(String novelId, WebSocketSession session) {
        connections.computeIfAbsent(novelId, k -> new ArrayList<>()).add(session);
        log.debug("WebSocket connected for novel {}: {}", novelId, session.getId());
    }

    /**
     * Disconnect a WebSocket session from a novel.
     */
    public void disconnect(String novelId, WebSocketSession session) {
        List<WebSocketSession> conns = connections.get(novelId);
        if (conns != null) {
            conns.remove(session);
            log.debug("WebSocket disconnected for novel {}: {}", novelId, session.getId());
        }
    }

    /**
     * Broadcast data to all connected sessions for a novel.
     */
    public void broadcast(String novelId, Map<String, Object> data) {
        List<WebSocketSession> conns = connections.get(novelId);
        if (conns == null || conns.isEmpty()) {
            return;
        }

        List<WebSocketSession> dead = new ArrayList<>();
        // Inject novel_id so the frontend can filter stale/cross-novel messages
        Map<String, Object> payload = new java.util.HashMap<>(data);
        payload.put("novel_id", novelId);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(jsonPayload);

            for (WebSocketSession ws : conns) {
                try {
                    if (ws.isOpen()) {
                        ws.sendMessage(message);
                    } else {
                        dead.add(ws);
                    }
                } catch (IOException e) {
                    log.warn("Failed to send message to WebSocket session {}: {}", ws.getId(), e.getMessage());
                    dead.add(ws);
                }
            }

            // Remove dead connections
            conns.removeAll(dead);
            if (dead.size() > 0) {
                log.debug("Removed {} dead WebSocket connections for novel {}", dead.size(), novelId);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast message for novel {}: {}", novelId, e.getMessage());
        }
    }

    /**
     * Get the number of connected sessions for a novel.
     */
    public int getConnectionCount(String novelId) {
        List<WebSocketSession> conns = connections.get(novelId);
        return conns != null ? conns.size() : 0;
    }

    /**
     * Clean up all connections for a novel.
     */
    public void cleanup(String novelId) {
        connections.remove(novelId);
        log.debug("Cleaned up all connections for novel {}", novelId);
    }
}