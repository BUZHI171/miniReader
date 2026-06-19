package com.aireader.v2.websocket;

import com.aireader.v2.service.QueryService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
    private final QueryService queryService;
    
    // 存储每个小说的聊天WebSocket连接
    private final Map<String, Set<WebSocketSession>> connections = new ConcurrentHashMap<>();

    @Data
    public static class ChatMessage {
        @JsonProperty("novel_id")
        private String novelId;
        private String question;
        @JsonProperty("conversation_id")
        private String conversationId;
    }

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
        String payload = message.getPayload();
        log.debug("收到聊天消息: {}", payload);

        try {
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
            
            if (chatMessage.getNovelId() == null || chatMessage.getQuestion() == null) {
                sendError(session, "Missing novel_id or question");
                return;
            }

            // 处理查询
            queryService.queryStream(
                    chatMessage.getNovelId(),
                    chatMessage.getQuestion(),
                    chatMessage.getConversationId(),
                    new QueryService.QueryCallback() {
                        @Override
                        public void onToken(String token) {
                            sendMessage(session, Map.of(
                                    "type", "token",
                                    "content", token
                            ));
                        }

                        @Override
                        public void onSources(List<Integer> chapters) {
                            sendMessage(session, Map.of(
                                    "type", "sources",
                                    "chapters", chapters
                            ));
                        }

                        @Override
                        public void onComplete() {
                            sendMessage(session, Map.of("type", "done"));
                        }

                        @Override
                        public void onError(String error) {
                            sendError(session, error);
                        }
                    }
            );

        } catch (Exception e) {
            log.error("处理聊天消息失败: {}", e.getMessage(), e);
            sendError(session, "Invalid JSON: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        for (Set<WebSocketSession> sessions : connections.values()) {
            sessions.remove(session);
        }
        log.info("Chat WebSocket连接已关闭: status={}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Chat WebSocket传输错误: {}", exception.getMessage());
    }

    /**
     * 发送流式响应片段
     */
    public void sendChunk(String novelId, String chunk) {
        Set<WebSocketSession> sessions = connections.get(novelId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        sendMessageToSessions(sessions, Map.of(
                "type", "chunk",
                "content", chunk
        ));
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> data) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String message = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        sendMessage(session, Map.of(
                "type", "error",
                "message", errorMessage
        ));
        sendMessage(session, Map.of("type", "done"));
    }

    private void sendMessageToSessions(Set<WebSocketSession> sessions, Map<String, Object> data) {
        try {
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
