package com.aireader.v2.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天WebSocket处理器
 * 处理流式对话
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // 存储每个小说的聊天WebSocket连接
    private final Map<String, Set<WebSocketSession>> connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String novelId = extractNovelId(query);
        
        if (novelId != null) {
            connections.computeIfAbsent(novelId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
            log.info("Chat WebSocket连接已建立: novelId={}", novelId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理聊天消息
        log.debug("收到聊天消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        for (Set<WebSocketSession> sessions : connections.values()) {
            sessions.remove(session);
        }
        log.info("Chat WebSocket连接已关闭: status={}", status);
    }

    /**
     * 发送流式响应片段
     */
    public void sendChunk(String novelId, String chunk) {
        Set<WebSocketSession> sessions = connections.get(novelId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> data = Map.of(
                    "type", "chunk",
                    "content", chunk
            );
            String message = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("发送聊天消息失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("序列化聊天消息失败: {}", e.getMessage());
        }
    }

    private String extractNovelId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "novelId".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }
}
