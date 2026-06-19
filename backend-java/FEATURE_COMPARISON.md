# Python与Java后端功能对比清单

> 生成时间: 2026-06-19
> 项目: AI Reader V2

---

## 一、小说管理功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 小说列表 | 获取所有小说列表，包含进度信息 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P0 |
| 小说详情 | 获取单个小说详情 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P0 |
| 删除小说 | 删除小说及所有关联数据 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P0 |
| 小说统计 | 获取小说分析统计信息 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P1 |
| 小说简介获取 | 获取小说简介 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 小说简介更新 | 更新小说简介 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 小说简介生成 | 使用LLM生成小说简介 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |

---

## 二、文件上传与解析

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 文件上传预览 | 上传.txt/.md文件并返回章节分割预览 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P0 |
| 分割模式列表 | 获取可用的章节分割模式 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 重新分割章节 | 使用不同模式重新分割章节 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 获取原始文本 | 获取缓存的原始文本 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 智能推断分割 | 从用户标记的分割点推断正则模式 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 清理并重分割 | 清理文本噪音并重新分割章节 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P2 |
| 确认导入 | 确认导入已上传的文件 | `backend/src/api/routes/novels.py` | ✓ 已实现 | P1 |

---

## 三、章节管理功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 章节列表 | 获取所有章节及分析状态 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P0 |
| 章节内容 | 获取单个章节内容 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P0 |
| 章节实体 | 获取章节中的实体名称 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P1 |
| 章节搜索 | 全文本搜索章节 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P2 |
| 章节排除 | 批量排除或恢复章节 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P2 |
| 用户状态获取 | 获取用户阅读状态 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P1 |
| 用户状态保存 | 保存用户阅读位置 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P1 |
| 书签列表 | 获取所有书签 | `backend/src/api/routes/chapters.py` | ✓ 已实现(BookmarkController) | P2 |
| 创建书签 | 添加书签 | `backend/src/api/routes/chapters.py` | ✓ 已实现(BookmarkController) | P2 |
| 删除书签 | 删除书签 | `backend/src/api/routes/chapters.py` | ✓ 已实现(BookmarkController) | P2 |

---

## 四、分析功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取活跃分析 | 获取当前运行中的分析任务 | `backend/src/api/routes/analysis.py` | ✓ 已实现 | P1 |
| 成本估算 | 估算云LLM分析成本 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 开始分析 | 触发小说分析任务 | `backend/src/api/routes/analysis.py` | ✓ 已实现 | P1 |
| 任务控制 | 暂停/恢复/取消分析任务 | `backend/src/api/routes/analysis.py` | ✓ 已实现 | P1 |
| 获取任务状态 | 查询任务状态和进度 | `backend/src/api/routes/analysis.py` | ✓ 已实现 | P1 |
| 最新任务信息 | 获取最新分析任务及统计 | `backend/src/api/routes/analysis.py` | ✓ 已实现 | P1 |
| 重试失败章节 | 重试所有失败的章节 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 清除分析数据 | 清除所有分析数据 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 成本明细 | 获取每章节成本明细 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 成本CSV导出 | 导出演成本明细CSV | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 分析记录列表 | 列出所有已完成分析任务 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |

---

## 五、实体管理功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 实体列表 | 获取所有实体，可按类型筛选 | `backend/src/api/routes/entities.py` | ✓ 已实现(EntityController) | P1 |
| 实体详情 | 获取单个实体的聚合信息 | `backend/src/api/routes/entities.py` | ✓ 已实现(EntityController) | P1 |

---

## 六、世界结构功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取世界结构 | 获取小说的世界结构 | `backend/src/api/routes/world_structure.py` | ✓ 已实现(WorldStructureController) | P1 |
| 获取覆盖层 | 获取所有用户覆盖层 | `backend/src/api/routes/world_structure.py` | ✓ 已实现(WorldStructureController) | P2 |
| 保存覆盖层 | 批量保存覆盖层 | `backend/src/api/routes/world_structure.py` | ✓ 已实现(WorldStructureController) | P2 |
| 删除覆盖层 | 删除特定覆盖层 | `backend/src/api/routes/world_structure.py` | ✓ 已实现(WorldStructureController) | P2 |
| 层级重建 | 预览并重建层级结构(SSE) | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P1 |
| 应用层级变更 | 应用用户选择的层级变更 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P1 |
| 空间补全 | 运行空间补全Agent(SSE) | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
| 拓扑度量 | 计算拓扑质量度量 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
| 层级重建V2 | 使用GeoOrchestrator重建 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P1 |
| 版本历史 | 获取层级版本历史 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
| 版本回滚 | 回滚到特定版本 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |

---

## 七、地图可视化功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取地图数据 | 获取地图位置、轨迹等数据 | `backend/src/api/routes/map.py` | ✓ 已实现(MapController) | P1 |
| 保存位置覆盖 | 保存用户位置覆盖 | `backend/src/api/routes/map.py` | ✓ 已实现(MapController) | P2 |
| 获取地形图 | 获取生成的地形PNG | `backend/src/api/routes/map.py` | ✓ 已实现(MapController) | P2 |

---

## 八、关系图表功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取关系图 | 获取人物关系图数据 | `backend/src/api/routes/graph.py` | ✓ 已实现(GraphController) | P1 |

---

## 九、聊天功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 对话列表 | 获取所有对话 | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P1 |
| 创建对话 | 创建新对话 | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P1 |
| 删除对话 | 删除对话 | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P2 |
| 消息列表 | 获取对话消息 | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P1 |
| 导出对话 | 导出对话为Markdown | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P2 |
| 导出全部对话 | 导出小说所有对话 | `backend/src/api/routes/chat.py` | ✓ 已实现(ConversationController) | P2 |

---

## 十、百科功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 分类统计 | 获取百科分类统计 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P1 |
| 条目列表 | 获取百科条目 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P1 |
| 位置冲突 | 获取位置层级冲突 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P2 |
| 概念详情 | 获取概念详情 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P1 |
| 空间摘要 | 获取位置空间关系 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P2 |
| 实体场景 | 获取涉及实体的场景 | `backend/src/api/routes/encyclopedia.py` | ✓ 已实现(EncyclopediaController) | P2 |

---

## 十一、预扫描功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 触发预扫描 | 手动触发预扫描 | `backend/src/api/routes/prescan.py` | ✓ 已实现(PrescanController) | P2 |
| 预扫描状态 | 查询预扫描状态 | `backend/src/api/routes/prescan.py` | ✓ 已实现(PrescanController) | P2 |
| 实体词典 | 获取实体词典内容 | `backend/src/api/routes/prescan.py` | ✓ 已实现(PrescanController) | P2 |

---

## 十二、设置功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取设置 | 获取当前LLM配置 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P0 |
| 健康检查 | 检查LLM连接状态 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P0 |
| 启动Ollama | 尝试启动Ollama | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 硬件信息 | 获取系统硬件信息 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 模型推荐 | 获取推荐模型列表 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 拉取模型 | 拉取Ollama模型(SSE) | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 设置默认模型 | 设置默认Ollama模型 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云服务商列表 | 获取云LLM服务商列表 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 云配置获取 | 获取云LLM配置 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 云配置保存 | 保存云LLM配置 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云API验证 | 测试云LLM API连接 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 切换LLM模式 | 在Ollama和云LLM间切换 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 运行任务数 | 获取运行中任务数 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 恢复默认 | 恢复默认LLM配置 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 预算获取 | 获取月度预算信息 | `backend/src/api/routes/settings.py` | ✓ 已实现(SettingsController) | P2 |
| 预算保存 | 设置月度预算 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 模型基准测试 | 运行模型基准测试 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 基准测试历史 | 获取基准测试历史 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 删除基准记录 | 删除基准测试记录 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |

---

## 十三、备份导出功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 导出备份 | 导出所有小说为ZIP | `backend/src/api/routes/backup.py` | ✓ 已实现(BackupController) | P2 |
| 导入预览 | 预览备份ZIP内容 | `backend/src/api/routes/backup.py` | ✓ 已实现(BackupController) | P2 |
| 确认导入 | 从备份ZIP导入 | `backend/src/api/routes/backup.py` | ✓ 已实现(BackupController) | P2 |

---

## 十四、系列圣经功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 导出系列圣经 | 导出设定集 | `backend/src/api/routes/series_bible.py` | ✓ 已实现(SeriesBibleController) | P2 |
| 获取模板列表 | 获取可用的导出模板 | `backend/src/api/routes/series_bible.py` | ✓ 已实现(SeriesBibleController) | P2 |

---

## 十五、场景功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取场景列表 | 获取章节场景数据 | `backend/src/api/routes/scenes.py` | ✓ 已实现(SceneController) | P2 |

---

## 十六、WebSocket功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 分析进度WebSocket | 分析进度实时推送 | `backend/src/api/websocket/analysis_ws.py` | ✓ 已实现(AnalysisWebSocketHandler) | P1 |
| 聊天WebSocket | 聊天实时交互 | `backend/src/api/websocket/chat_ws.py` | ✓ 已实现(ChatWebSocketHandler) | P1 |

---

## 十七、数据库存储功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 小说存储 | 小说CRUD操作 | `backend/src/db/novel_store.py` | ✓ 已实现(JPA) | P0 |
| 章节存储 | 章节CRUD操作 | `backend/src/db/chapter_store.py` | ✓ 已实现(JPA) | P0 |
| 章节事实存储 | 章节分析结果存储 | `backend/src/db/chapter_fact_store.py` | ✓ 已实现(JPA) | P1 |
| 对话存储 | 对话和消息存储 | `backend/src/db/conversation_store.py` | ✓ 已实现(JPA) | P1 |
| 实体词典存储 | 实体词典管理 | `backend/src/db/entity_dictionary_store.py` | ✓ 已实现(JPA) | P2 |
| 分析任务存储 | 分析任务管理 | `backend/src/db/analysis_task_store.py` | ✓ 已实现(JPA) | P1 |
| 世界结构存储 | 世界结构管理 | `backend/src/db/world_structure_store.py` | ✓ 已实现(JPA) | P1 |
| 世界结构覆盖存储 | 覆盖层管理 | `backend/src/db/world_structure_override_store.py` | ✓ 已实现(JPA) | P2 |
| 使用事件存储 | 使用事件记录 | `backend/src/db/usage_event_store.py` | ✗ 未实现 | P2 |

---

## 十八、服务层功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 分析服务 | 章节分析核心逻辑 | `backend/src/services/analysis_service.py` | ✗ 未实现 | P1 |
| 实体聚合器 | 实体信息聚合 | `backend/src/services/entity_aggregator.py` | ✗ 未实现 | P1 |
| 百科服务 | 百科数据服务 | `backend/src/services/encyclopedia_service.py` | ✗ 未实现 | P1 |
| 可视化服务 | 图表和地图数据 | `backend/src/services/visualization_service.py` | ✗ 未实现 | P1 |
| 世界结构Agent | 世界结构管理Agent | `backend/src/services/world_structure_agent.py` | ✗ 未实现 | P1 |
| 冲突检测器 | 冲突检测 | `backend/src/services/conflict_detector.py` | ✗ 未实现 | P2 |
| 导出服务 | 数据导出 | `backend/src/services/export_service.py` | ✗ 未实现 | P2 |
| 备份服务 | 数据备份 | `backend/src/services/backup_service.py` | ✗ 未实现 | P2 |
| 章节分割服务 | 章节分割工具 | `backend/src/utils/chapter_splitter.py` | ✓ 已实现 | P0 |

---

## 十九、提取层功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 章节事实提取 | LLM章节分析 | `backend/src/extraction/chapter_fact_extractor.py` | ✗ 未实现 | P1 |
| 上下文摘要构建 | 构建上下文摘要 | `backend/src/extraction/context_summary_builder.py` | ✗ 未实现 | P1 |
| 实体预扫描 | 实体预扫描 | `backend/src/extraction/entity_pre_scanner.py` | ✗ 未实现 | P2 |
| 场景LLM提取 | 场景提取 | `backend/src/extraction/scene_llm_extractor.py` | ✗ 未实现 | P2 |
| 概要生成器 | LLM生成概要 | `backend/src/extraction/synopsis_generator.py` | ✗ 未实现 | P2 |

---

## 二十、基础设施功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| LLM客户端 | OpenAI兼容客户端 | `backend/src/infra/llm_client.py` | ✓ 已实现(CloudLlmClient) | P0 |
| Ollama客户端 | Ollama集成 | `backend/src/infra/llm_client.py` | ✓ 已实现(OllamaClient) | P0 |
| Anthropic客户端 | Claude集成 | `backend/src/infra/anthropic_client.py` | ✓ 已实现 | P2 |
| 配置管理 | 应用配置 | `backend/src/infra/config.py` | ✓ 已实现(LlmConfig) | P0 |
| 上下文预算 | 上下文管理 | `backend/src/infra/context_budget.py` | ✗ 未实现 | P2 |

---

## 功能实现统计

| 类别 | 总功能数 | 已实现 | 未实现 | 完成率 |
|-----|---------|-------|-------|--------|
| 小说管理 | 7 | 4 | 3 | 57% |
| 文件上传与解析 | 7 | 2 | 5 | 29% |
| 章节管理 | 10 | 10 | 0 | 100% |
| 分析功能 | 11 | 5 | 6 | 45% |
| 实体管理 | 2 | 2 | 0 | 100% |
| 世界结构 | 10 | 4 | 6 | 40% |
| 地图可视化 | 3 | 3 | 0 | 100% |
| 关系图表 | 1 | 1 | 0 | 100% |
| 聊天功能 | 6 | 6 | 0 | 100% |
| 百科功能 | 6 | 6 | 0 | 100% |
| 预扫描功能 | 3 | 3 | 0 | 100% |
| 设置功能 | 19 | 9 | 10 | 47% |
| 备份导出 | 3 | 3 | 0 | 100% |
| 系列圣经 | 2 | 2 | 0 | 100% |
| 场景功能 | 1 | 1 | 0 | 100% |
| WebSocket | 2 | 2 | 0 | 100% |
| 数据库存储 | 9 | 8 | 1 | 89% |
| 服务层 | 9 | 1 | 8 | 11% |
| 提取层 | 5 | 0 | 5 | 0% |
| 基础设施 | 5 | 4 | 1 | 80% |
| **总计** | **122** | **74** | **48** | **61%** |

---

## 建议开发优先级

### P0 - 核心基础（必须实现）
1. 小说列表/详情/删除 ✓ 已实现
2. 章节列表/内容 ✓ 已实现
3. 健康检查 ✓ 已实现
4. LLM客户端 ✓ 已实现

### P1 - 核心功能（应尽快实现）
1. **分析服务**（章节分析核心逻辑）- 核心功能，未实现
2. **实体聚合器** - 实体详情需要，未实现
3. **世界结构Agent** - 层级重建需要，未实现
4. **章节事实提取** - LLM章节分析，未实现
5. **上下文摘要构建** - 分析上下文管理，未实现
6. **重试失败章节** - 分析容错，未实现
7. **层级重建** - 世界结构核心功能，未实现
8. **应用层级变更** - 世界结构核心功能，未实现
9. **层级重建V2** - GeoOrchestrator，未实现

### P2 - 增强功能（后续迭代）
1. 搜索功能 ✓ 已实现
2. 书签管理 ✓ 已实现
3. 预扫描 ✓ 已实现
4. 地图可视化 ✓ 已实现
5. 备份导出 ✓ 已实现
6. 云LLM配置 - 部分实现
7. 成本估算 - 未实现
8. 预算管理 - 部分实现
9. 模型基准测试 - 未实现
10. 使用事件存储 - 未实现
11. 上下文预算 - 未实现

---

## Java后端已实现文件清单

| 文件 | 功能 |
|-----|------|
| `NovelController.java` | 小说API端点 |
| `AnalysisController.java` | 分析任务管理 |
| `ConversationController.java` | 聊天对话管理 |
| `SettingsController.java` | 设置功能 |
| `BackupController.java` | 备份管理 |
| `BookmarkController.java` | 书签管理 |
| `WorldStructureController.java` | 世界结构管理 |
| `EntityController.java` | 实体管理 |
| `EncyclopediaController.java` | 百科功能 |
| `GraphController.java` | 关系图表 |
| `MapController.java` | 地图可视化 |
| `PrescanController.java` | 预扫描 |
| `SceneController.java` | 场景管理 |
| `SeriesBibleController.java` | 系列圣经 |
| `HealthController.java` | 健康检查 |
| `AnalysisWebSocketHandler.java` | 分析进度WebSocket |
| `ChatWebSocketHandler.java` | 聊天WebSocket |
| `NovelService.java` | 小说业务逻辑 |
| `QueryService.java` | 查询服务 |
| `OllamaClient.java` | Ollama客户端 |
| `CloudLlmClient.java` | 云LLM客户端 |
| `ChapterSplitter.java` | 章节分割工具 |
| `Novel.java` | 小说实体 |
| `Chapter.java` | 章节实体 |
| `ChapterFact.java` | 章节事实实体 |
| `AnalysisTask.java` | 分析任务实体 |
| `Conversation.java` | 对话实体 |
| `Message.java` | 消息实体 |
| `Bookmark.java` | 书签实体 |
| `EntityDictionary.java` | 实体词典实体 |
| `UserState.java` | 用户状态实体 |
| `WorldStructure.java` | 世界结构实体 |
| `NovelRepository.java` | 小说数据访问 |
| `ChapterRepository.java` | 章节数据访问 |
| `ChapterFactRepository.java` | 章节事实数据访问 |
| `AnalysisTaskRepository.java` | 分析任务数据访问 |
| `ConversationRepository.java` | 对话数据访问 |
| `MessageRepository.java` | 消息数据访问 |
| `BookmarkRepository.java` | 书签数据访问 |
| `EntityDictionaryRepository.java` | 实体词典数据访问 |
| `UserStateRepository.java` | 用户状态数据访问 |
| `WorldStructureRepository.java` | 世界结构数据访问 |
| `CorsConfig.java` | 跨域配置 |
| `WebSocketConfig.java` | WebSocket配置 |
| `LlmConfig.java` | LLM配置 |
| `AiReaderApplication.java` | 应用入口 |

---

## Python后端核心文件清单

| 文件/目录 | 功能 |
|----------|------|
| `api/routes/` | API路由（17个路由文件） |
| `api/websocket/` | WebSocket处理 |
| `api/schemas/` | Pydantic模式 |
| `db/` | 数据库存储层（9个存储文件） |
| `services/` | 业务逻辑服务（20+服务） |
| `extraction/` | LLM提取逻辑（6个文件） |
| `infra/` | 基础设施（6个文件） |
| `models/` | 数据模型（4个文件） |
| `utils/` | 工具函数（7个文件） |

---

## 下一步开发任务计划

### 第一阶段：核心分析服务实现（2周）

| 任务ID | 任务名称 | 目标描述 | 优先级 | 时间节点 | 责任人 |
|--------|---------|---------|--------|---------|--------|
| P1-001 | 实现AnalysisService | 章节分析核心逻辑，包含任务调度、LLM调用、结果存储 | P1 | 第1周 | 后端开发组 |
| P1-002 | 实现ChapterFactExtractor | LLM章节分析提取器 | P1 | 第1周 | 后端开发组 |
| P1-003 | 实现ContextSummaryBuilder | 上下文摘要构建器 | P1 | 第1周 | 后端开发组 |
| P1-004 | 实现EntityAggregator | 实体信息聚合服务 | P1 | 第2周 | 后端开发组 |
| P1-005 | 实现EncyclopediaService | 百科数据服务 | P1 | 第2周 | 后端开发组 |
| P1-006 | 实现VisualizationService | 图表和地图数据服务 | P1 | 第2周 | 后端开发组 |

### 第二阶段：世界结构服务实现（2周）

| 任务ID | 任务名称 | 目标描述 | 优先级 | 时间节点 | 责任人 |
|--------|---------|---------|--------|---------|--------|
| P1-007 | 实现WorldStructureAgent | 世界结构管理Agent核心逻辑 | P1 | 第3周 | 后端开发组 |
| P1-008 | 实现层级重建API | 预览并重建层级结构(SSE) | P1 | 第3周 | 后端开发组 |
| P1-009 | 实现应用层级变更API | 应用用户选择的层级变更 | P1 | 第3周 | 后端开发组 |
| P1-010 | 实现GeoOrchestrator | 空间技能编排器 | P1 | 第4周 | 后端开发组 |
| P1-011 | 实现空间补全API | 运行空间补全Agent(SSE) | P2 | 第4周 | 后端开发组 |

### 第三阶段：设置功能完善（1周）

| 任务ID | 任务名称 | 目标描述 | 优先级 | 时间节点 | 责任人 |
|--------|---------|---------|--------|---------|--------|
| P2-001 | 实现Ollama模型拉取 | 拉取Ollama模型(SSE) | P2 | 第5周 | 后端开发组 |
| P2-002 | 实现设置默认模型 | 设置默认Ollama模型 | P2 | 第5周 | 后端开发组 |
| P2-003 | 实现云配置保存 | 保存云LLM配置 | P2 | 第5周 | 后端开发组 |
| P2-004 | 实现LLM模式切换 | 在Ollama和云LLM间切换 | P2 | 第5周 | 后端开发组 |

### 第四阶段：高级功能实现（1周）

| 任务ID | 任务名称 | 目标描述 | 优先级 | 时间节点 | 责任人 |
|--------|---------|---------|--------|---------|--------|
| P2-005 | 实现成本估算API | 估算云LLM分析成本 | P2 | 第6周 | 后端开发组 |
| P2-006 | 实现成本明细API | 获取每章节成本明细 | P2 | 第6周 | 后端开发组 |
| P2-007 | 实现预算保存 | 设置月度预算 | P2 | 第6周 | 后端开发组 |
| P2-008 | 实现上下文预算管理 | 上下文窗口管理 | P2 | 第6周 | 后端开发组 |

### 第五阶段：测试与集成（1周）

| 任务ID | 任务名称 | 目标描述 | 优先级 | 时间节点 | 责任人 |
|--------|---------|---------|--------|---------|--------|
| P0-001 | 单元测试覆盖 | 核心服务单元测试 | P0 | 第7周 | 测试组 |
| P0-002 | 集成测试 | API集成测试 | P0 | 第7周 | 测试组 |
| P0-003 | 性能测试 | LLM分析性能测试 | P1 | 第7周 | 测试组 |

### 里程碑总结

| 阶段 | 时间跨度 | 关键交付物 |
|-----|---------|-----------|
| 第一阶段 | 第1-2周 | 核心分析服务完成 |
| 第二阶段 | 第3-4周 | 世界结构服务完成 |
| 第三阶段 | 第5周 | 设置功能完善 |
| 第四阶段 | 第6周 | 高级功能完成 |
| 第五阶段 | 第7周 | 测试验收 |

---

## 代码参考

Python后端核心文件位置：
- [novels.py](file:///e:/kaifa/other/miniReader/backend/src/api/routes/novels.py)
- [chapters.py](file:///e:/kaifa/other/miniReader/backend/src/api/routes/chapters.py)
- [analysis.py](file:///e:/kaifa/other/miniReader/backend/src/api/routes/analysis.py)
- [world_structure.py](file:///e:/kaifa/other/miniReader/backend/src/api/routes/world_structure.py)

Java后端核心文件位置：
- [NovelController.java](file:///e:/kaifa/other/miniReader/backend-java/src/main/java/com/aireader/v2/controller/NovelController.java)
- [AnalysisController.java](file:///e:/kaifa/other/miniReader/backend-java/src/main/java/com/aireader/v2/controller/AnalysisController.java)
- [ConversationController.java](file:///e:/kaifa/other/miniReader/backend-java/src/main/java/com/aireader/v2/controller/ConversationController.java)
- [SettingsController.java](file:///e:/kaifa/other/miniReader/backend-java/src/main/java/com/aireader/v2/controller/SettingsController.java)