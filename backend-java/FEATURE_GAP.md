# Java 后端功能缺失记录

本文档记录 Java 版后端与 Python 版后端的功能差异，特别是尚未完整实现的部分。

---

## 1. ContextBuilder (对应 Python: `context_summary_builder.py`)

**文件位置**: `backend-java/src/main/java/com/aireader/v2/service/ContextBuilder.java`

| 功能 | Python 方法 | Java 实现 | 状态 |
|-----|------------|----------|------|
| 主入口 | `build()` | `buildContext()` | ⚠️ 简化版 |
| 聚合人物 | `_aggregate_characters()` | ✅ 已实现 | ✅ |
| 聚合地点 | `_aggregate_locations()` | ✅ 已实现 | ✅ |
| 聚合关系 | `_aggregate_relationships()` | ❌ 未实现 | 🔴 |
| 聚合物品 | `_aggregate_items()` | ❌ 未实现 | 🔴 |
| 场景焦点 | `_build_scene_focus_section()` | ❌ 未实现 | 🔴 |
| 宏观枢纽 | `_build_macro_hub_section()` | ❌ 未实现 | 🔴 |
| 层级链 | `_format_hierarchy_chains()` | ❌ 未实现 | 🔴 |
| 地理状态 | `_build_geo_state_section()` | ❌ 未实现 | 🔴 |
| 世界结构 | `_build_world_structure_section()` | ❌ 未实现 | 🔴 |
| 实体字典 | `_build_dictionary_section()` | ❌ 未实现 | 🔴 |
| 层级链构建 | `_build_upward_chain()` | ❌ 未实现 | 🔴 |

**影响**: LLM 提取时缺少上下文信息，可能导致提取质量下降。

---

## 2. AnalysisService (对应 Python: `analysis_service.py`)

**文件位置**: `backend-java/src/main/java/com/aireader/v2/service/AnalysisService.java`

| 功能 | Python 实现 | Java 实现 | 状态 |
|-----|------------|----------|------|
| 任务生命周期 | ✅ | ✅ | ✅ |
| 章节循环处理 | ✅ | ✅ | ✅ |
| 上下文构建 | ✅ | ⚠️ 简化版 | ⚠️ |
| LLM 事实提取 | ✅ | ✅ | ✅ |
| 事实验证 | `validate_fact()` | Stub | 🔴 |
| 名称解析 | `resolve_names()` | Stub | 🔴 |
| 世界结构更新 | `update_world_structure()` | Stub | 🔴 |
| 场景提取 | `extract_scenes()` | Stub | 🔴 |
| 嵌入索引 | `index_embeddings()` | Stub | 🔴 |
| 地点层级优化 | `enhance_location_hierarchy()` | Stub | 🔴 |
| 概要生成 | `auto_generate_synopsis()` | Stub | 🔴 |
| 自动层级重建 | `auto_rebuild_hierarchy()` | Stub | 🔴 |
| 自动空间补全 | `auto_spatial_completion()` | Stub | 🔴 |
| 错误分类 | ✅ | ✅ | ✅ |
| 自动重试 | ✅ | ✅ | ✅ |

---

## 3. FactExtractor (对应 Python: `chapter_fact_extractor.py`)

**文件位置**: `backend-java/src/main/java/com/aireader/v2/service/FactExtractor.java`

| 功能 | Python 实现 | Java 实现 | 状态 |
|-----|------------|----------|------|
| LLM 调用 | ✅ | ✅ | ✅ |
| Prompt 构建 | ✅ | ⚠️ 简化版 | ⚠️ |
| JSON 解析 | ✅ | ✅ | ✅ |
| 结构化输出 | 完整 Schema | 简化版 | ⚠️ |

**Python 完整输出结构**:
```json
{
  "characters": [{"name": "", "new_aliases": [], "abilities_gained": [], "locations_in_chapter": []}],
  "locations": [{"name": "", "type": "", "parent": "", "role": ""}],
  "relationships": [{"person_a": "", "person_b": "", "relation_type": ""}],
  "events": [{"summary": "", "participants": [], "location": "", "chapter_num": 0}],
  "item_events": [{"item_name": "", "item_type": "", "action": "", "actor": "", "recipient": ""}]
}
```

**Java 简化版输出结构**:
```json
{
  "characters": ["人物1", "人物2"],
  "locations": ["地点1", "地点2"],
  "relationships": [],
  "events": []
}
```

---

## 4. SceneExtractor (对应 Python: `scene_extractor.py`)

**文件位置**: `backend-java/src/main/java/com/aireader/v2/service/SceneExtractor.java`

| 功能 | Python 实现 | Java 实现 | 状态 |
|-----|------------|----------|------|
| 多信号边界评分 | ✅ | ✅ | ✅ |
| 时间检测 | ✅ | ✅ | ✅ |
| 情感基调检测 | ✅ | ✅ | ✅ |
| 对话检测 | ✅ | ✅ | ✅ |
| 事件-段落映射 | `_map_events_to_paragraphs()` | ❌ 未实现 | 🔴 |
| 角色分类 | `_classify_character_roles()` | ❌ 未实现 | 🔴 |
| 事件类型分类 | `_classify_event_type()` | ❌ 未实现 | 🔴 |

---

## 5. HierarchyOptimizer (对应 Python: `hierarchy_consolidator.py`)

**文件位置**: `backend-java/src/main/java/com/aireader/v2/service/HierarchyOptimizer.java`

| 功能 | Python 实现 | Java 实现 | 状态 |
|-----|------------|----------|------|
| 收集地点 | ✅ | ✅ | ✅ |
| 构建层级 | ✅ | ⚠️ 简化版 | ⚠️ |
| LLM 层级审查 | `LocationHierarchyReviewer` | ❌ 未实现 | 🔴 |
| 场景转移分析 | `SceneTransitionAnalyzer` | ❌ 未实现 | 🔴 |
| 层级整合 | `consolidate_hierarchy()` | ❌ 未实现 | 🔴 |

---

## 6. 其他缺失服务

| Python 服务 | 功能 | Java 实现 | 状态 |
|------------|------|----------|------|
| `scene_llm_extractor.py` | LLM 场景提取 | ❌ 未实现 | 🔴 |
| `fact_validator.py` | 事实验证 | ❌ 未实现 | 🔴 |
| `name_resolver.py` | 名称解析 | ❌ 未实现 | 🔴 |
| `embedding_service.py` | 嵌入索引 | ❌ 未实现 | 🔴 |
| `world_structure_agent.py` | 世界结构代理 | ⚠️ 部分实现 | ⚠️ |
| `visualization_service.py` | 可视化服务 | ⚠️ Timeline 已实现 | ⚠️ |
| `usage/tracking` | 使用追踪 | ❌ 未实现 | 🔴 |

---

## 7. API 接口对比

| API 端点 | Python | Java | 状态 |
|----------|--------|------|------|
| `GET /api/novels/{id}/timeline` | ✅ | ✅ 已实现 | ✅ |
| `GET /api/usage/track` | ✅ | ❌ 未实现 | 🔴 |
| `GET /api/novels/{id}/graph` | ✅ | ❌ 未实现 | 🔴 |
| `GET /api/novels/{id}/factions` | ✅ | ❌ 未实现 | 🔴 |
| `GET /api/novels/{id}/map` | ✅ | ❌ 未实现 | 🔴 |
| `POST /api/novels/{id}/series-bible/export` | ✅ | ⚠️ 部分实现 | ⚠️ |

---

## 优先级建议

### P0 - 核心功能（影响基本使用）
1. `ContextBuilder` 完整实现 - 影响 LLM 提取质量
2. `FactExtractor` 结构化输出 - 影响数据完整性

### P1 - 重要功能（影响用户体验）
1. `AnalysisService` stub 方法实现
2. `SceneExtractor` 事件-段落映射
3. `HierarchyOptimizer` LLM 层级审查

### P2 - 增强功能（可后续迭代）
1. 嵌入索引服务
2. 世界结构代理完善
3. 概要生成优化

---

## 更新日志

| 日期 | 更新内容 |
|-----|---------|
| 2026-06-20 | 初始创建，记录 ContextBuilder、AnalysisService、FactExtractor 等功能差异 |
