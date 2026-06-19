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
| 小说统计 | 获取小说分析统计信息 | `backend/src/api/routes/novels.py` | ✗ 未实现 | P1 |
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
| 确认导入 | 确认导入已上传的文件 | `backend/src/api/routes/novels.py` | ✓ 部分实现 | P1 |

---

## 三、章节管理功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 章节列表 | 获取所有章节及分析状态 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P0 |
| 章节内容 | 获取单个章节内容 | `backend/src/api/routes/chapters.py` | ✓ 已实现 | P0 |
| 章节实体 | 获取章节中的实体名称 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P1 |
| 章节搜索 | 全文本搜索章节 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P2 |
| 章节排除 | 批量排除或恢复章节 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P2 |
| 用户状态获取 | 获取用户阅读状态 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P1 |
| 用户状态保存 | 保存用户阅读位置 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P1 |
| 书签列表 | 获取所有书签 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P2 |
| 创建书签 | 添加书签 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P2 |
| 删除书签 | 删除书签 | `backend/src/api/routes/chapters.py` | ✗ 未实现 | P2 |

---

## 四、分析功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取活跃分析 | 获取当前运行中的分析任务 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 成本估算 | 估算云LLM分析成本 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 开始分析 | 触发小说分析任务 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 任务控制 | 暂停/恢复/取消分析任务 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 获取任务状态 | 查询任务状态和进度 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 最新任务信息 | 获取最新分析任务及统计 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 重试失败章节 | 重试所有失败的章节 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P1 |
| 清除分析数据 | 清除所有分析数据 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 成本明细 | 获取每章节成本明细 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 成本CSV导出 | 导出演成本明细CSV | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |
| 分析记录列表 | 列出所有已完成分析任务 | `backend/src/api/routes/analysis.py` | ✗ 未实现 | P2 |

---

## 五、实体管理功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 实体列表 | 获取所有实体，可按类型筛选 | `backend/src/api/routes/entities.py` | ✗ 未实现 | P1 |
| 实体详情 | 获取单个实体的聚合信息 | `backend/src/api/routes/entities.py` | ✗ 未实现 | P1 |

---

## 六、世界结构功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取世界结构 | 获取小说的世界结构 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P1 |
| 获取覆盖层 | 获取所有用户覆盖层 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
| 保存覆盖层 | 批量保存覆盖层 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
| 删除覆盖层 | 删除特定覆盖层 | `backend/src/api/routes/world_structure.py` | ✗ 未实现 | P2 |
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
| 获取地图数据 | 获取地图位置、轨迹等数据 | `backend/src/api/routes/map.py` | ✗ 未实现 | P1 |
| 保存位置覆盖 | 保存用户位置覆盖 | `backend/src/api/routes/map.py` | ✗ 未实现 | P2 |
| 获取地形图 | 获取生成的地形PNG | `backend/src/api/routes/map.py` | ✗ 未实现 | P2 |

---

## 八、关系图表功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取关系图 | 获取人物关系图数据 | `backend/src/api/routes/graph.py` | ✗ 未实现 | P1 |

---

## 九、聊天功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 对话列表 | 获取所有对话 | `backend/src/api/routes/chat.py` | ✓ 已实现 | P1 |
| 创建对话 | 创建新对话 | `backend/src/api/routes/chat.py` | ✓ 已实现 | P1 |
| 删除对话 | 删除对话 | `backend/src/api/routes/chat.py` | ✓ 已实现 | P2 |
| 消息列表 | 获取对话消息 | `backend/src/api/routes/chat.py` | ✓ 已实现 | P1 |
| 导出对话 | 导出对话为Markdown | `backend/src/api/routes/chat.py` | ✓ 已实现 | P2 |
| 导出全部对话 | 导出小说所有对话 | `backend/src/api/routes/chat.py` | ✓ 已实现 | P2 |

---

## 十、百科功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 分类统计 | 获取百科分类统计 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P1 |
| 条目列表 | 获取百科条目 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P1 |
| 位置冲突 | 获取位置层级冲突 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P2 |
| 概念详情 | 获取概念详情 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P1 |
| 空间摘要 | 获取位置空间关系 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P2 |
| 实体场景 | 获取涉及实体的场景 | `backend/src/api/routes/encyclopedia.py` | ✗ 未实现 | P2 |

---

## 十一、预扫描功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 触发预扫描 | 手动触发预扫描 | `backend/src/api/routes/prescan.py` | ✗ 未实现 | P2 |
| 预扫描状态 | 查询预扫描状态 | `backend/src/api/routes/prescan.py` | ✗ 未实现 | P2 |
| 实体词典 | 获取实体词典内容 | `backend/src/api/routes/prescan.py` | ✗ 未实现 | P2 |

---

## 十二、设置功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 获取设置 | 获取当前LLM配置 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P0 |
| 健康检查 | 检查LLM连接状态 | `backend/src/api/routes/settings.py` | ✓ 部分实现 | P0 |
| 启动Ollama | 尝试启动Ollama | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 硬件信息 | 获取系统硬件信息 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 模型推荐 | 获取推荐模型列表 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 拉取模型 | 拉取Ollama模型(SSE) | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 设置默认模型 | 设置默认Ollama模型 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云服务商列表 | 获取云LLM服务商列表 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云配置获取 | 获取云LLM配置 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云配置保存 | 保存云LLM配置 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 云API验证 | 测试云LLM API连接 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 切换LLM模式 | 在Ollama和云LLM间切换 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 运行任务数 | 获取运行中任务数 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 恢复默认 | 恢复默认LLM配置 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 预算获取 | 获取月度预算信息 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 预算保存 | 设置月度预算 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 模型基准测试 | 运行模型基准测试 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 基准测试历史 | 获取基准测试历史 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |
| 删除基准记录 | 删除基准测试记录 | `backend/src/api/routes/settings.py` | ✗ 未实现 | P2 |

---

## 十三、备份导出功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 导出备份 | 导出所有小说为ZIP | `backend/src/api/routes/backup.py` | ✗ 未实现 | P2 |
| 导入预览 | 预览备份ZIP内容 | `backend/src/api/routes/backup.py` | ✗ 未实现 | P2 |
| 确认导入 | 从备份ZIP导入 | `backend/src/api/routes/backup.py` | ✗ 未实现 | P2 |

---

## 十四、其他导出功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 导出系列圣经 | 导出设定集 | `backend/src/api/routes/series_bible.py` | ✗ 未实现 | P2 |
| 导出小说 | 导出小说数据 | `backend/src/api/routes/export_import.py` | ✗ 未实现 | P2 |
| 导出AIR格式 | 导出AIR格式 | `backend/src/api/routes/export_import.py` | ✗ 未实现 | P2 |
| 导入预览 | 预览数据导入 | `backend/src/api/routes/export_import.py` | ✗ 未实现 | P2 |
| 确认导入 | 确认数据导入 | `backend/src/api/routes/export_import.py` | ✗ 未实现 | P2 |

---

## 十五、WebSocket功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 分析进度WebSocket | 分析进度实时推送 | `backend/src/api/websocket/analysis_ws.py` | ✗ 未实现 | P1 |
| 聊天WebSocket | 聊天实时交互 | `backend/src/api/websocket/chat_ws.py` | ✗ 未实现 | P1 |

---

## 十六、数据库存储功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 小说存储 | 小说CRUD操作 | `backend/src/db/novel_store.py` | ✓ 已实现(JPA) | P0 |
| 章节存储 | 章节CRUD操作 | `backend/src/db/chapter_store.py` | ✓ 已实现(JPA) | P0 |
| 章节事实存储 | 章节分析结果存储 | `backend/src/db/chapter_fact_store.py` | ✗ 未实现 | P1 |
| 对话存储 | 对话和消息存储 | `backend/src/db/conversation_store.py` | ✗ 未实现 | P1 |
| 实体词典存储 | 实体词典管理 | `backend/src/db/entity_dictionary_store.py` | ✗ 未实现 | P2 |
| 分析任务存储 | 分析任务管理 | `backend/src/db/analysis_task_store.py` | ✗ 未实现 | P1 |
| 世界结构存储 | 世界结构管理 | `backend/src/db/world_structure_store.py` | ✗ 未实现 | P1 |
| 世界结构覆盖存储 | 覆盖层管理 | `backend/src/db/world_structure_override_store.py` | ✗ 未实现 | P2 |
| 使用事件存储 | 使用事件记录 | `backend/src/db/usage_event_store.py` | ✗ 未实现 | P2 |

---

## 十七、服务层功能

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

---

## 十八、提取层功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| 章节事实提取 | LLM章节分析 | `backend/src/extraction/chapter_fact_extractor.py` | ✗ 未实现 | P1 |
| 上下文摘要构建 | 构建上下文摘要 | `backend/src/extraction/context_summary_builder.py` | ✗ 未实现 | P1 |
| 实体预扫描 | 实体预扫描 | `backend/src/extraction/entity_pre_scanner.py` | ✗ 未实现 | P2 |
| 场景LLM提取 | 场景提取 | `backend/src/extraction/scene_llm_extractor.py` | ✗ 未实现 | P2 |
| 概要生成器 | LLM生成概要 | `backend/src/extraction/synopsis_generator.py` | ✗ 未实现 | P2 |

---

## 十九、基础设施功能

| 功能名称 | 功能描述 | Python实现文件 | Java实现状态 | 优先级 |
|---------|---------|--------------|-------------|--------|
| LLM客户端 | OpenAI兼容客户端 | `backend/src/infra/llm_client.py` | ✓ 部分实现 | P0 |
| Ollama客户端 | Ollama集成 | `backend/src/infra/llm_client.py` | ✓ 部分实现 | P0 |
| Anthropic客户端 | Claude集成 | `backend/src/infra/anthropic_client.py` | ✗ 未实现 | P2 |
| 配置管理 | 应用配置 | `backend/src/infra/config.py` | ✓ 部分实现 | P0 |
| 上下文预算 | 上下文管理 | `backend/src/infra/context_budget.py` | ✗ 未实现 | P2 |

---

## 功能实现统计

| 类别 | 总功能数 | 已实现 | 未实现 | 完成率 |
|-----|---------|-------|-------|--------|
| 小说管理 | 7 | 3 | 4 | 43% |
| 文件上传与解析 | 7 | 2 | 5 | 29% |
| 章节管理 | 10 | 2 | 8 | 20% |
| 分析功能 | 11 | 0 | 11 | 0% |
| 实体管理 | 2 | 0 | 2 | 0% |
| 世界结构 | 10 | 0 | 10 | 0% |
| 地图可视化 | 3 | 0 | 3 | 0% |
| 关系图表 | 1 | 0 | 1 | 0% |
| 聊天功能 | 6 | 0 | 6 | 0% |
| 百科功能 | 6 | 0 | 6 | 0% |
| 预扫描功能 | 3 | 0 | 3 | 0% |
| 设置功能 | 19 | 1 | 18 | 5% |
| 备份导出 | 3 | 0 | 3 | 0% |
| 其他导出 | 4 | 0 | 4 | 0% |
| WebSocket | 2 | 0 | 2 | 0% |
| 数据库存储 | 9 | 2 | 7 | 22% |
| 服务层 | 8 | 0 | 8 | 0% |
| 提取层 | 5 | 0 | 5 | 0% |
| 基础设施 | 5 | 3 | 2 | 60% |
| **总计** | **121** | **13** | **108** | **11%** |

---

## 建议开发优先级

### P0 - 核心基础（必须实现）
1. 小说列表/详情/删除 ✓ 已实现
2. 章节列表/内容 ✓ 已实现
3. 健康检查 ✓ 部分实现
4. LLM客户端 ✓ 部分实现

### P1 - 核心功能（应尽快实现）
1. 分析服务（分析任务管理）
2. 实体管理（实体列表/详情）
3. 用户状态管理（阅读进度）
4. 百科功能（条目/详情）
5. 世界结构（获取/重建）
6. 关系图（人物关系）
7. 对话管理

### P2 - 增强功能（后续迭代）
1. 搜索功能
2. 书签管理
3. 预扫描
4. 地图可视化
5. 备份导出
6. 模型基准测试
7. 云LLM配置

---

## Java后端已实现文件清单

| 文件 | 功能 |
|-----|------|
| `NovelController.java` | 小说API端点 |
| `HealthController.java` | 健康检查端点 |
| `NovelService.java` | 小说业务逻辑 |
| `Novel.java` | 小说实体 |
| `Chapter.java` | 章节实体 |
| `NovelDTO.java` | 小说数据传输对象 |
| `UploadPreviewResponse.java` | 上传预览响应 |
| `NovelRepository.java` | 小说数据访问 |
| `ChapterRepository.java` | 章节数据访问 |
| `CorsConfig.java` | 跨域配置 |
| `LlmConfig.java` | LLM配置 |
| `OllamaClient.java` | Ollama客户端 |
| `ChapterSplitter.java` | 章节分割工具 |
| `AiReaderApplication.java` | 应用入口 |

---

## Python后端核心文件清单

| 文件/目录 | 功能 |
|----------|------|
| `api/routes/` | API路由 |
| `api/websocket/` | WebSocket处理 |
| `api/schemas/` | Pydantic模式 |
| `db/` | 数据库存储层 |
| `services/` | 业务逻辑服务 |
| `extraction/` | LLM提取逻辑 |
| `infra/` | 基础设施 |
| `models/` | 数据模型 |
