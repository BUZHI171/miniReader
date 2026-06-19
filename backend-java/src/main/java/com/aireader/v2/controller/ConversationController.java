package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Conversation;
import com.aireader.v2.model.entity.Message;
import com.aireader.v2.repository.ConversationRepository;
import com.aireader.v2.repository.MessageRepository;
import com.aireader.v2.repository.NovelRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NovelRepository novelRepository;
    private final ObjectMapper objectMapper;

    @Data
    public static class CreateConversationRequest {
        private String title = "新对话";
    }

    @Data
    public static class ConversationWithMessageCount {
        @JsonProperty("id")
        private String id;

        @JsonProperty("novel_id")
        private String novelId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("message_count")
        private Long messageCount;
    }

    @Data
    public static class MessageWithSources {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("conversation_id")
        private String conversationId;

        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private String content;

        @JsonProperty("sources")
        private List<Integer> sources;

        @JsonProperty("created_at")
        private String createdAt;
    }

    // GET /api/novels/{novelId}/conversations
    @GetMapping("/api/novels/{novelId}/conversations")
    public ResponseEntity<Map<String, Object>> listConversations(@PathVariable String novelId) {
        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        List<Object[]> results = conversationRepository.findConversationsWithMessageCount(novelId);
        List<ConversationWithMessageCount> conversations = results.stream()
                .map(row -> {
                    ConversationWithMessageCount c = new ConversationWithMessageCount();
                    c.setId((String) row[0]);
                    c.setNovelId((String) row[1]);
                    c.setTitle((String) row[2]);
                    c.setCreatedAt((String) row[3]);
                    c.setUpdatedAt((String) row[4]);
                    c.setMessageCount(((Number) row[5]).longValue());
                    return c;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("conversations", conversations);
        return ResponseEntity.ok(response);
    }

    // POST /api/novels/{novelId}/conversations
    @PostMapping("/api/novels/{novelId}/conversations")
    public ResponseEntity<Conversation> createConversation(
            @PathVariable String novelId,
            @RequestBody CreateConversationRequest request) {
        
        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .novelId(novelId)
                .title(request.getTitle())
                .createdAt(now)
                .updatedAt(now)
                .build();

        conversationRepository.save(conversation);
        return ResponseEntity.ok(conversation);
    }

    // DELETE /api/conversations/{conversationId}
    @DeleteMapping("/api/conversations/{conversationId}")
    public ResponseEntity<Map<String, Boolean>> deleteConversation(@PathVariable String conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            return ResponseEntity.notFound().build();
        }

        conversationRepository.deleteById(conversationId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);
        return ResponseEntity.ok(response);
    }

    // GET /api/conversations/{conversationId}/messages
    @GetMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> listMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "100") int limit) {
        
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Message> messages = messageRepository.findByConversationIdWithLimit(conversationId, limit);
        List<MessageWithSources> messagesWithSources = messages.stream()
                .map(this::convertToMessageWithSources)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messagesWithSources);
        response.put("conversation", convOpt.get());
        return ResponseEntity.ok(response);
    }

    // GET /api/conversations/{conversationId}/export
    @GetMapping("/api/conversations/{conversationId}/export")
    public ResponseEntity<String> exportConversation(@PathVariable String conversationId) {
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conversation conv = convOpt.get();
        List<Message> messages = messageRepository.findByConversationIdWithLimit(conversationId, 10000);

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(conv.getTitle()).append("\n\n");
        sb.append("> 创建于 ").append(conv.getCreatedAt()).append("  \n");
        sb.append("> 共 ").append(messages.size()).append(" 条消息\n\n");
        sb.append("---\n\n");

        for (Message msg : messages) {
            String roleLabel = "user".equals(msg.getRole()) ? "**用户**" : "**AI**";
            sb.append("### ").append(roleLabel).append("\n\n");
            sb.append(msg.getContent()).append("\n");

            List<Integer> sources = parseSourcesJson(msg.getSourcesJson());
            if (!sources.isEmpty()) {
                String chaptersStr = sources.stream()
                        .map(ch -> "第" + ch + "章")
                        .collect(Collectors.joining(", "));
                sb.append("\n*来源: ").append(chaptersStr).append("*\n");
            }
            sb.append("\n");
        }

        String filename = conv.getTitle().replace("\"", "'");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".md\"")
                .contentType(MediaType.parseMediaType("text/markdown; charset=utf-8"))
                .body(sb.toString());
    }

    // GET /api/novels/{novelId}/conversations/export
    @GetMapping("/api/novels/{novelId}/conversations/export")
    public ResponseEntity<String> exportAllConversations(@PathVariable String novelId) {
        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        List<Object[]> results = conversationRepository.findConversationsWithMessageCount(novelId);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# 全部对话记录\n\n");
        sb.append("> 共 ").append(results.size()).append(" 个对话\n\n");

        for (Object[] row : results) {
            String convId = (String) row[0];
            String title = (String) row[2];
            String createdAt = (String) row[3];

            sb.append("---\n\n");
            sb.append("## ").append(title).append("\n\n");
            sb.append("> 创建于 ").append(createdAt).append("\n\n");

            List<Message> messages = messageRepository.findByConversationIdWithLimit(convId, 10000);
            for (Message msg : messages) {
                String roleLabel = "user".equals(msg.getRole()) ? "**用户**" : "**AI**";
                sb.append("### ").append(roleLabel).append("\n\n");
                sb.append(msg.getContent()).append("\n");

                List<Integer> sources = parseSourcesJson(msg.getSourcesJson());
                if (!sources.isEmpty()) {
                    String chaptersStr = sources.stream()
                            .map(ch -> "第" + ch + "章")
                            .collect(Collectors.joining(", "));
                    sb.append("\n*来源: ").append(chaptersStr).append("*\n");
                }
                sb.append("\n");
            }
        }

        sb.append("---\n\n*由 AI Reader V2 导出*\n");

        String filename = URLEncoder.encode("全部对话.md", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("text/markdown; charset=utf-8"))
                .body(sb.toString());
    }

    private MessageWithSources convertToMessageWithSources(Message msg) {
        MessageWithSources m = new MessageWithSources();
        m.setId(msg.getId());
        m.setConversationId(msg.getConversationId());
        m.setRole(msg.getRole());
        m.setContent(msg.getContent());
        m.setSources(parseSourcesJson(msg.getSourcesJson()));
        m.setCreatedAt(msg.getCreatedAt());
        return m;
    }

    private List<Integer> parseSourcesJson(String sourcesJson) {
        if (sourcesJson == null || sourcesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(sourcesJson, new TypeReference<List<Integer>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse sources_json: {}", sourcesJson, e);
            return Collections.emptyList();
        }
    }
}
