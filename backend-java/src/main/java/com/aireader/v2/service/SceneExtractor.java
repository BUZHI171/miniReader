package com.aireader.v2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Scene extractor — split chapters into scenes for screenplay mode.
 * 
 * Uses a multi-signal scoring system to detect scene boundaries in Chinese
 * narrative text. No LLM needed — purely rule-based splitting.
 * 
 * Boundary signals and weights:
 *   - Narrator transition (却说/话说/且说/再说/单说)     5
 *   - Scene closure (不在话下/按下不表/暂且不提)          4
 *   - Time jump (次日/三日后/是夜/翌晨/过了数日)         4
 *   - Scene opening (但见/只见/来到/行至/进入)            3
 *   - Blank-line gap (2+ consecutive blank lines)       3
 *   - Dialogue cluster boundary (≥3 lines mode switch)  2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneExtractor {

    private final ObjectMapper objectMapper;

    // ── Boundary signal patterns ─────────────────────

    // Weight 5 — narrator transition phrases (classic Chinese storytelling)
    private static final Pattern NARRATOR_TRANSITION = Pattern.compile(
        "^(?:却说|话说|且说|再说|单说|单表|再表|且表|却表|话表|" +
        "且不说|再不说|暂不说|只说|先说|后说|" +
        "却道是|正是|有诗为证|有词为证|" +
        "欲知后事|未知|毕竟)"
    );

    // Weight 4 — scene closure phrases
    private static final Pattern SCENE_CLOSURE = Pattern.compile(
        "(?:不在话下|按下不表|暂且不提|此处不提|不必细说|不必多言|" +
        "言归正传|闲话少叙|闲言少叙|此是后话|容后再叙|" +
        "后事如何|且听下回|这且不说|不[在题])[。，。,]?"
    );

    // Chapter ending pattern — should be merged into previous scene
    private static final Pattern CHAPTER_ENDING = Pattern.compile(
        "^(?:毕竟|欲知后事|且听下回|正是|有诗为证|未知)"
    );

    // Weight 4 — time jump expressions
    private static final Pattern TIME_JUMP = Pattern.compile(
        "^(?:次日|翌日|明日|隔日|过了[一二三四五六七八九十百千数几]?[日天年月]|" +
        "[一二三四五六七八九十]日后|数日后|[一二三四五六七八九十百千]年后|" +
        "是[日夜晚]|当[夜晚日]|那[日夜晚天]|到了[第那]|" +
        "翌[日晨]|清[晨早]|黄昏|傍晚|入夜|深夜|半夜|三更|" +
        "天[明亮]|天色[微渐]|日[出落]|月[上升]|" +
        "过了[一数几]会[儿]?|过了半[天晌日]|" +
        "又过了|时光荏苒|光阴似箭|" +
        "不[多觉][久时]|转眼[间之]?|不一[会时日]|须臾|片刻之?后|少顷|" +
        "一日|这一日|忽一日|有一日|" +
        "却早过了|不觉[过已]了|不觉倏)"
    );

    // Weight 2 — weaker time signals at paragraph start
    private static final Pattern TIME_WEAK = Pattern.compile(
        "^(?:当[时下]|此时|这时|彼时|那时|" +
        "少[时顷]间|一[时连]间)"
    );

    // Weight 3 — internal time jump (within first 60 chars)
    private static final Pattern INTERNAL_TIME_JUMP = Pattern.compile(
        "(?:。|，|；)(?:一日[，,。]|这一日|忽一日|有一日|" +
        "却早过了|不觉[已过]了[一二三四五六七八九十百千数几]+[年月日]|" +
        "何期有[一二三四五六七八九十百千数几]+[年月日载百])"
    );

    // Weight 3 — scene opening phrases
    private static final Pattern SCENE_OPENING = Pattern.compile(
        "^(?:但见|只见|来到|行至|进入|走进|来至|赶到|回到|去到|" +
        "走到|飞到|奔到|到了|到得|径[直奔往]|一路|" +
        "忽[然见听闻]|猛然|突然|蓦然|陡然|倏然|" +
        "原来|不想|不料|谁[知想料]|哪[知想料]|" +
        "好[猴行大圣]|这[猴行大圣]|那[猴行大圣]|" +
        "这一去|也是他|正[是行走说])"
    );

    // ── Time-of-day detection ─────────────────────────

    private static final Pattern TIME_MORNING = Pattern.compile(
        "清[晨早]|早[上晨间]|天[明亮]|晨[光曦]|旭日|日出|卯时|辰时|拂晓|黎明|破晓"
    );
    private static final Pattern TIME_NOON = Pattern.compile(
        "午[时后间]|正午|中午|日[正中]|巳时|午时|晌午|日头正"
    );
    private static final Pattern TIME_EVENING = Pattern.compile(
        "黄昏|傍晚|日落|日[暮薄]|夕[阳照]|申时|酉时|薄暮|暮色"
    );
    private static final Pattern TIME_NIGHT = Pattern.compile(
        "[深半]夜|入夜|夜[间里晚色深幕]|月[色光上]|星[光辰]|三更|" +
        "戌时|亥时|子时|丑时|寅时|漆黑|灯火"
    );

    // ── Emotional tone keywords ──────────────────────

    private static final Pattern TONE_BATTLE = Pattern.compile(
        "杀[了来去死过将]|打[了来去将杀斗]|[大激鏖]战|恶斗|" +
        "一[刀剑枪棒拳掌]|交[手战锋]|厮[杀打]|" +
        "攻[击打]|抵[挡御]|格[挡斗]|流血|负伤|怒[吼喝骂]|" +
        "砍[了去来]|刺[了去来]|挡[了住开]"
    );
    private static final Pattern TONE_SAD = Pattern.compile(
        "[哭泣悲伤]|落[泪下]泪|流泪|痛[哭苦]|悲[痛伤戚]|哀[伤痛号]|" +
        "凄[惨凉]|惨|伤心|难过|感[伤怀]"
    );
    private static final Pattern TONE_HAPPY = Pattern.compile(
        "[笑喜乐]|欢[喜乐笑]|高兴|快[乐活]|大喜|开心|" +
        "庆[祝贺]|贺|喜悦|欣喜|兴奋"
    );
    private static final Pattern TONE_TENSE = Pattern.compile(
        "紧张|危[急险]|急[忙切]|惊[恐慌险惧吓]|恐[惧怖]|" +
        "[逃跑躲闪藏]|追[赶杀来]|险[些要]|命悬|千钧一发|" +
        "心[惊慌跳]|冷汗|倒吸|不[妙好敢]"
    );

    // ── Dialogue detection ────────────────────────────

    private static final String DIALOGUE_STARTERS = "\"\'「『";

    // ── Cache ─────────────────────────────────────────

    private final Map<String, Map<Integer, List<Scene>>> sceneCache = new HashMap<>();

    public void invalidateSceneCache(String novelId) {
        sceneCache.remove(novelId);
    }

    // ── Main entry points ────────────────────────────

    /**
     * Extract scenes from a single chapter.
     */
    public List<Scene> extractScenes(String content, String title, int chapterNum, Map<String, Object> factData) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        return splitIntoScenes(content, title, chapterNum, factData);
    }

    /**
     * Extract scenes from chapter content (without fact data).
     */
    public List<Scene> extractScenes(String content, int chapterNum) {
        return extractScenes(content, "第" + chapterNum + "章", chapterNum, null);
    }

    // ── Core splitting algorithm ─────────────────────

    private List<Scene> splitIntoScenes(String content, String chapterTitle, int chapterNum, Map<String, Object> factData) {
        // Split content preserving blank lines for gap detection
        String[] rawLines = content.split("\n");

        // Build paragraph list with original line indices
        List<String> paragraphs = new ArrayList<>();
        List<Integer> paraLineIndices = new ArrayList<>();

        for (int i = 0; i < rawLines.length; i++) {
            String stripped = rawLines[i].trim();
            if (!stripped.isEmpty()) {
                paragraphs.add(stripped);
                paraLineIndices.add(i);
            }
        }

        if (paragraphs.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect fact data
        List<?> events = factData != null ? (List<?>) factData.getOrDefault("events", Collections.emptyList()) : Collections.emptyList();
        List<?> characters = factData != null ? (List<?>) factData.getOrDefault("characters", Collections.emptyList()) : Collections.emptyList();
        List<?> locations = factData != null ? (List<?>) factData.getOrDefault("locations", Collections.emptyList()) : Collections.emptyList();

        Set<String> charNames = new HashSet<>();
        for (Object ch : characters) {
            if (ch instanceof Map) {
                Map<?, ?> chMap = (Map<?, ?>) ch;
                String name = (String) chMap.get("name");
                if (name != null) charNames.add(name);
                List<?> aliases = (List<?>) chMap.get("new_aliases");
                if (aliases != null) {
                    for (Object alias : aliases) {
                        if (alias != null) charNames.add(alias.toString());
                    }
                }
            } else if (ch != null) {
                charNames.add(ch.toString());
            }
        }

        List<String> locNames = new ArrayList<>();
        for (Object loc : locations) {
            if (loc instanceof Map) {
                Map<?, ?> locMap = (Map<?, ?>) loc;
                String name = (String) locMap.get("name");
                if (name != null) locNames.add(name);
            } else if (loc != null) {
                locNames.add(loc.toString());
            }
        }

        // Compute boundary scores
        List<Double> boundaryScores = computeBoundaryScores(paragraphs, paraLineIndices, rawLines);

        // Determine threshold — adaptive based on chapter length
        int baseThreshold;
        if (paragraphs.size() > 50) {
            baseThreshold = 3;
        } else if (paragraphs.size() > 20) {
            baseThreshold = 4;
        } else if (paragraphs.size() > 12) {
            baseThreshold = 5;
        } else {
            baseThreshold = 6;
        }

        // Find scene break points
        List<Integer> breakPoints = findBreakPoints(boundaryScores, baseThreshold, 3);

        // Build scenes from break points
        List<Scene> scenes = buildScenesFromBreaks(paragraphs, breakPoints, chapterNum, charNames, locNames);

        // Fallback: if still only 1 scene, try progressively lower thresholds
        if (scenes.size() <= 1 && paragraphs.size() > 8) {
            for (int drop = 1; drop <= 2; drop++) {
                int lowerThreshold = Math.max(baseThreshold - drop, 3);
                if (lowerThreshold >= baseThreshold) continue;
                List<Integer> lowerBreaks = findBreakPoints(boundaryScores, lowerThreshold, 3);
                if (lowerBreaks.size() > breakPoints.size()) {
                    scenes = buildScenesFromBreaks(paragraphs, lowerBreaks, chapterNum, charNames, locNames);
                    break;
                }
            }
        }

        // Merge trivial trailing scenes (chapter ending phrases)
        if (scenes.size() > 1) {
            Scene last = scenes.get(scenes.size() - 1);
            int[] pr = last.getParagraphRange();
            int lastParaCount = pr[1] - pr[0] + 1;
            if (lastParaCount <= 2) {
                String lastText = String.join("\n", paragraphs.subList(pr[0], pr[1] + 1));
                if (CHAPTER_ENDING.matcher(lastText).find() || lastParaCount == 1) {
                    // Merge into previous scene
                    Scene prev = scenes.get(scenes.size() - 2);
                    int[] prevPr = prev.getParagraphRange();
                    prev.setParagraphRange(new int[]{prevPr[0], pr[1]});
                    scenes.remove(scenes.size() - 1);
                }
            }
        }

        return scenes;
    }

    private List<Double> computeBoundaryScores(List<String> paragraphs, List<Integer> paraLineIndices, String[] rawLines) {
        int n = paragraphs.size();
        List<Double> scores = new ArrayList<>(Collections.nCopies(n, 0.0));

        // Track dialogue mode
        List<Boolean> isDialogueList = new ArrayList<>();
        for (String p : paragraphs) {
            isDialogueList.add(isDialogue(p));
        }

        for (int i = 0; i < n; i++) {
            String para = paragraphs.get(i);
            double score = 0.0;

            // Signal 1: Narrator transition (weight 5)
            if (NARRATOR_TRANSITION.matcher(para).find()) {
                score += 5;
            }

            // Signal 2: Previous paragraph has scene closure (weight 4)
            if (i > 0 && SCENE_CLOSURE.matcher(paragraphs.get(i - 1)).find()) {
                score += 4;
            }

            // Signal 3: Time jump (weight 4)
            if (TIME_JUMP.matcher(para).find()) {
                score += 4;
            }

            // Signal 4: Scene opening phrase (weight 3)
            if (SCENE_OPENING.matcher(para).find()) {
                score += 3;
            }

            // Signal 4b: Weak time signal at start (weight 2)
            if (TIME_WEAK.matcher(para).find()) {
                score += 2;
            }

            // Signal 4c: Internal time jump in first 60 chars (weight 3)
            if (!TIME_JUMP.matcher(para).find() && para.length() > 0) {
                String prefix = para.substring(0, Math.min(60, para.length()));
                if (INTERNAL_TIME_JUMP.matcher(prefix).find()) {
                    score += 3;
                }
            }

            // Signal 5: Blank-line gap (weight 3)
            if (i > 0) {
                int currentLineIdx = paraLineIndices.get(i);
                int prevLineIdx = paraLineIndices.get(i - 1);
                int blankCount = currentLineIdx - prevLineIdx - 1;
                if (blankCount >= 2) {
                    score += 3;
                }
            }

            // Signal 6: Dialogue cluster boundary (weight 2)
            if (i >= 3) {
                boolean prevDialogue = isDialogueList.get(i - 1) && isDialogueList.get(i - 2) && isDialogueList.get(i - 3);
                if (prevDialogue && !isDialogueList.get(i)) {
                    int fwdNarration = 0;
                    for (int j = i; j < Math.min(i + 3, n); j++) {
                        if (!isDialogueList.get(j)) fwdNarration++;
                    }
                    if (fwdNarration >= 2) {
                        score += 2;
                    }
                }

                boolean prevNarration = !isDialogueList.get(i - 1) && !isDialogueList.get(i - 2) && !isDialogueList.get(i - 3);
                if (prevNarration && isDialogueList.get(i)) {
                    int fwdDialogue = 0;
                    for (int j = i; j < Math.min(i + 3, n); j++) {
                        if (isDialogueList.get(j)) fwdDialogue++;
                    }
                    if (fwdDialogue >= 2) {
                        score += 2;
                    }
                }
            }

            scores.set(i, score);
        }

        return scores;
    }

    private List<Integer> findBreakPoints(List<Double> scores, double threshold, int minSceneParas) {
        List<Integer> breaks = new ArrayList<>();
        breaks.add(0);

        for (int i = 1; i < scores.size(); i++) {
            if (scores.get(i) >= threshold) {
                // Strong boundaries allow shorter preceding scenes
                int minDist = scores.get(i) >= 7 ? 2 : minSceneParas;
                if (i - breaks.get(breaks.size() - 1) >= minDist) {
                    breaks.add(i);
                }
            }
        }

        return breaks;
    }

    private List<Scene> buildScenesFromBreaks(List<String> paragraphs, List<Integer> breakPoints, 
                                               int chapterNum, Set<String> charNames, List<String> locNames) {
        List<Scene> scenes = new ArrayList<>();

        for (int idx = 0; idx < breakPoints.size(); idx++) {
            int start = breakPoints.get(idx);
            int end = (idx + 1 < breakPoints.size()) ? breakPoints.get(idx + 1) : paragraphs.size();
            List<String> sceneParas = paragraphs.subList(start, end);

            Scene scene = buildRichScene(idx, chapterNum, sceneParas, new int[]{start, end - 1}, charNames, locNames);
            scenes.add(scene);
        }

        return scenes;
    }

    // ── Rich scene metadata builders ─────────────────

    private Scene buildRichScene(int index, int chapterNum, List<String> paragraphs, int[] paragraphRange,
                                  Set<String> charNames, List<String> locNames) {
        String text = String.join("\n", paragraphs);

        // Characters present in this scene
        List<String> presentChars = new ArrayList<>();
        for (String c : charNames) {
            if (text.contains(c)) {
                presentChars.add(c);
            }
        }

        // Location
        String sceneLoc = "";
        for (String loc : locNames) {
            if (text.contains(loc)) {
                sceneLoc = loc;
                break;
            }
        }

        // Heading (first non-dialogue sentence, truncated)
        String heading = extractHeading(paragraphs);

        // Title
        String title = heading != null && !heading.isEmpty() ? heading : "场景 " + (index + 1);

        // Time of day
        String timeOfDay = detectTimeOfDay(text);

        // Emotional tone
        String emotionalTone = detectEmotionalTone(text);

        // Key dialogue
        List<String> keyDialogue = extractKeyDialogue(paragraphs);

        // Dialogue count
        int dialogueCount = countDialogue(paragraphs);

        // Description
        String description = paragraphs.isEmpty() ? "" : truncate(paragraphs.get(0), 100);

        Scene scene = new Scene();
        scene.setIndex(index);
        scene.setChapter(chapterNum);
        scene.setTitle(title);
        scene.setLocation(sceneLoc);
        scene.setCharacters(presentChars.subList(0, Math.min(10, presentChars.size())));
        scene.setDescription(description);
        scene.setDialogueCount(dialogueCount);
        scene.setParagraphRange(paragraphRange);
        scene.setHeading(heading);
        scene.setTimeOfDay(timeOfDay);
        scene.setEmotionalTone(emotionalTone);
        scene.setKeyDialogue(keyDialogue);

        return scene;
    }

    private String extractHeading(List<String> paragraphs) {
        for (String para : paragraphs) {
            if (!isDialogue(para) && para.length() > 5) {
                // Find first sentence
                int end = para.indexOf('。');
                if (end > 0 && end < 50) {
                    return para.substring(0, end + 1);
                }
                return truncate(para, 30);
            }
        }
        return "";
    }

    private String detectTimeOfDay(String text) {
        if (TIME_MORNING.matcher(text).find()) return "早晨";
        if (TIME_NOON.matcher(text).find()) return "中午";
        if (TIME_EVENING.matcher(text).find()) return "傍晚";
        if (TIME_NIGHT.matcher(text).find()) return "夜晚";
        return "";
    }

    private String detectEmotionalTone(String text) {
        if (TONE_BATTLE.matcher(text).find()) return "战斗";
        if (TONE_SAD.matcher(text).find()) return "悲伤";
        if (TONE_HAPPY.matcher(text).find()) return "欢乐";
        if (TONE_TENSE.matcher(text).find()) return "紧张";
        return "";
    }

    private List<String> extractKeyDialogue(List<String> paragraphs) {
        List<String> dialogues = new ArrayList<>();
        for (String para : paragraphs) {
            if (isDialogue(para)) {
                dialogues.add(para);
                if (dialogues.size() >= 2) break;
            }
        }
        return dialogues;
    }

    private boolean isDialogue(String para) {
        if (para == null || para.isEmpty()) return false;
        char first = para.charAt(0);
        return DIALOGUE_STARTERS.indexOf(first) >= 0;
    }

    private int countDialogue(List<String> paragraphs) {
        int count = 0;
        for (String p : paragraphs) {
            if (isDialogue(p)) count++;
        }
        return count;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    // ── Scene data class ─────────────────────────────

    @Data
    public static class Scene {
        private Integer index;
        private Integer chapter;
        private String title;
        private String location = "";
        private List<String> characters = new ArrayList<>();
        private String description = "";
        private Integer dialogueCount = 0;
        private int[] paragraphRange;
        private String heading;
        private String timeOfDay;
        private String emotionalTone;
        private List<String> keyDialogue = new ArrayList<>();
        private List<CharacterRole> characterRoles = new ArrayList<>();
        private String eventType;
        private String summary;
    }

    @Data
    public static class CharacterRole {
        private String name;
        private String role;
    }
}