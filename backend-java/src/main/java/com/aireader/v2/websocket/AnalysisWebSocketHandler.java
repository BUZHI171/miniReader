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
 * 分析进度WebSocket处理器
 * 对应Python的Analysis WS
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // 存储每个小说的WebSocket连接
    private final Map<String, Set<WebSocketSession>> connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数获取novelId
        String query = session.getUri().getQuery();
        String novelId = extractNovelId(query);
        
        if (novelId != null) {
            connections.computeIfAbsent(novelId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
            log.info("WebSocket连接已建立: novelId={}", novelId);
        } else {
            log.warn("WebSocket连接缺少novelId参数");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端消息（如果需要）
        log.debug("收到消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除断开连接的session
        for (Set<WebSocketSession> sessions : connections.values()) {
            sessions.remove(session);
        }
        log.info("WebSocket连接已关闭: status={}", status);
    }

    /**
     * 广播消息到指定小说的所有连接
     */
    public void broadcast(String novelId, Map<String, Object> data) {
        Set<WebSocketSession> sessions = connections.get(novelId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // 添加novelId到消息中
        data.put("novel_id", novelId);

        try {
            String message = objectMapper.writeValueAsString(data);
            TextMessage textMessage = new TextMessage(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("发送WebSocket消息失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("序列化WebSocket消息失败: {}", e.getMessage());
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
