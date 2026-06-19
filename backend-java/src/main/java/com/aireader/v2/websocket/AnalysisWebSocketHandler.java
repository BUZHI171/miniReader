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
import java.util.HashMap;
import java.util.List;
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
            log.info("Analysis WebSocket连接已建立: novelId={}", novelId);
        } else {
            log.warn("Analysis WebSocket连接缺少novelId参数");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端消息（如果需要）
        log.debug("收到Analysis消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除断开连接的session
        for (Set<WebSocketSession> sessions : connections.values()) {
            sessions.remove(session);
        }
        log.info("Analysis WebSocket连接已关闭: status={}", status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Analysis WebSocket传输错误: {}", exception.getMessage());
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
        Map<String, Object> payload = new HashMap<>(data);
        payload.put("novel_id", novelId);

        try {
            String message = objectMapper.writeValueAsString(payload);
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

    /**
     * 发送进度更新
     */
    public void sendProgress(String novelId, int current, int total, String status, String currentChapter) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "progress");
        data.put("current", current);
        data.put("total", total);
        data.put("status", status);
        data.put("current_chapter", currentChapter);
        data.put("progress", total > 0 ? (double) current / total * 100 : 0);
        broadcast(novelId, data);
    }

    /**
     * 发送章节完成通知
     */
    public void sendChapterComplete(String novelId, int chapterNum, boolean success, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "chapter_complete");
        data.put("chapter_num", chapterNum);
        data.put("success", success);
        data.put("message", message);
        broadcast(novelId, data);
    }

    /**
     * 发送分析开始通知
     */
    public void sendAnalysisStarted(String novelId, int totalChapters) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "started");
        data.put("total_chapters", totalChapters);
        broadcast(novelId, data);
    }

    /**
     * 发送分析完成通知
     */
    public void sendAnalysisComplete(String novelId, int successCount, int failCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "complete");
        data.put("success_count", successCount);
        data.put("fail_count", failCount);
        broadcast(novelId, data);
    }

    /**
     * 发送分析错误通知
     */
    public void sendAnalysisError(String novelId, String error) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "error");
        data.put("error", error);
        broadcast(novelId, data);
    }

    /**
     * 发送分析暂停通知
     */
    public void sendAnalysisPaused(String novelId) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "paused");
        broadcast(novelId, data);
    }

    /**
     * 发送分析恢复通知
     */
    public void sendAnalysisResumed(String novelId) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "resumed");
        broadcast(novelId, data);
    }

    /**
     * 发送分析取消通知
     */
    public void sendAnalysisCancelled(String novelId) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "cancelled");
        broadcast(novelId, data);
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
