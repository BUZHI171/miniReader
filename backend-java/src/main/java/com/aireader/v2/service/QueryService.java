package com.aireader.v2.service;

import com.aireader.v2.llm.OllamaClient;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.model.entity.Message;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 查询服务
 * 处理小说问答功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final OllamaClient ollamaClient;
    private final ChapterFactRepository chapterFactRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    private static final String QA_SYSTEM_PROMPT = """
你是一个专业的小说分析助手。你的任务是根据提供的小说知识库信息，回答用户关于小说内容的问题。

## 规则
1. **严格基于提供的知识库信息回答**，绝对不要使用你自己的知识补充任何人物、情节或关系。如果知识库中没有提到，就不要提及。
2. 回答时引用来源章节，格式为 [第X章]
3. 如果信息不足以回答问题，诚实说明"根据已分析的内容，暂未找到相关信息"，不要猜测或补充
4. 回答要简洁明了，重点突出
5. 在回答中提到人物、地点、物品等实体时，用其原名

## 知识库信息
%s

## 对话历史
%s

请严格基于以上知识库信息回答用户的问题。不要添加知识库中未提及的内容。""";

    /**
     * 流式查询回调接口
     */
    public interface QueryCallback {
        void onToken(String token);
        void onSources(List<Integer> chapters);
        void onComplete();
        void onError(String error);
    }

    /**
     * 执行流式查询
     */
    public void queryStream(
            String novelId,
            String question,
            String conversationId,
            QueryCallback callback) {

        try {
            // 1. 加载章节事实
            List<ChapterFact> facts = chapterFactRepository.findByNovelId(novelId);
            if (facts.isEmpty()) {
                callback.onToken("该小说尚未进行分析，请先分析后再提问。");
                callback.onSources(Collections.emptyList());
                callback.onComplete();
                return;
            }

            // 2. 构建上下文
            String context = buildContext(facts, question);

            // 3. 获取对话历史
            String history = buildHistory(conversationId);

            // 4. 构建提示词
            String systemPrompt = String.format(QA_SYSTEM_PROMPT, context, history);
            String userPrompt = question + "\n\n（注：当前已分析 " + facts.size() + " 章内容）";

            // 5. 调用LLM流式生成
            StringBuilder fullAnswer = new StringBuilder();

            ollamaClient.generateStream("qwen2.5:14b", systemPrompt + "\n\n" + userPrompt, new OllamaClient.LlmStreamCallback() {
                @Override
                public void onChunk(String chunk) {
                    fullAnswer.append(chunk);
                    callback.onToken(chunk);
                }

                @Override
                public void onError(String error) {
                    log.error("LLM生成错误: {}", error);
                    callback.onError(error);
                }

                @Override
                public void onComplete() {
                    // 提取来源章节
                    List<Integer> sources = extractSourceChapters(fullAnswer.toString());

                    // 保存消息
                    if (conversationId != null) {
                        saveMessages(conversationId, question, fullAnswer.toString(), sources);
                    }

                    callback.onSources(sources);
                    callback.onComplete();
                }
            });

        } catch (Exception e) {
            log.error("查询处理失败: {}", e.getMessage(), e);
            callback.onError(e.getMessage());
        }
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<ChapterFact> facts, String question) {
        StringBuilder context = new StringBuilder();
        int totalChars = 0;
        int maxChars = 8000;

        // 简单实现：提取包含问题关键词的事实
        Set<String> keywords = Arrays.stream(question.split("[，。？！\\s]+"))
                .filter(w -> w.length() >= 2)
                .collect(Collectors.toSet());

        for (ChapterFact fact : facts) {
            String factJson = fact.getFactJson();
            if (factJson == null) continue;

            // 检查是否包含关键词
            boolean relevant = keywords.stream().anyMatch(kw -> factJson.contains(kw));

            if (relevant || totalChars < 2000) { // 至少包含一些上下文
                String snippet = "[第" + fact.getChapterId() + "章] " + truncateJson(factJson, 500);
                if (totalChars + snippet.length() > maxChars) break;
                context.append(snippet).append("\n");
                totalChars += snippet.length();
            }
        }

        return context.length() > 0 ? context.toString() : "（暂无相关知识库信息）";
    }

    /**
     * 构建对话历史
     */
    private String buildHistory(String conversationId) {
        if (conversationId == null) {
            return "（无历史对话）";
        }

        List<Message> messages = messageRepository.findTop10ByConversationIdOrderByIdDesc(conversationId);
        if (messages.isEmpty()) {
            return "（无历史对话）";
        }

        // 反转顺序（从旧到新）
        Collections.reverse(messages);

        StringBuilder history = new StringBuilder();
        for (Message msg : messages) {
            String role = "user".equals(msg.getRole()) ? "用户" : "助手";
            history.append(role).append(": ").append(truncate(msg.getContent(), 200)).append("\n");
        }

        return history.toString();
    }

    /**
     * 提取来源章节
     */
    private List<Integer> extractSourceChapters(String answer) {
        Pattern pattern = Pattern.compile("第(\\d+)章");
        Matcher matcher = pattern.matcher(answer);
        Set<Integer> chapters = new TreeSet<>();
        while (matcher.find()) {
            chapters.add(Integer.parseInt(matcher.group(1)));
        }
        return new ArrayList<>(chapters);
    }

    /**
     * 保存消息
     */
    private void saveMessages(String conversationId, String question, String answer, List<Integer> sources) {
        try {
            // 保存用户消息
            Message userMsg = Message.builder()
                    .conversationId(conversationId)
                    .role("user")
                    .content(question)
                    .build();
            messageRepository.save(userMsg);

            // 保存助手消息
            String sourcesJson = objectMapper.writeValueAsString(sources);
            Message assistantMsg = Message.builder()
                    .conversationId(conversationId)
                    .role("assistant")
                    .content(answer)
                    .sourcesJson(sourcesJson)
                    .build();
            messageRepository.save(assistantMsg);

        } catch (JsonProcessingException e) {
            log.error("保存消息失败: {}", e.getMessage());
        }
    }

    private String truncateJson(String json, int maxLen) {
        if (json == null) return "";
        return json.length() > maxLen ? json.substring(0, maxLen) + "..." : json;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
