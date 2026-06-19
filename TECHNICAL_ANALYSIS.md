# AI Reader V2 技术栈分析与架构梳理

---

## 1. 项目概述

**AI Reader V2** 是一款开源的 AI 小说分析可视化工具，支持上传任意 TXT/Markdown 小说，通过 LLM 自动提取人物关系、地点层级、事件时间线，生成交互式知识图谱、世界地图、时间线等多维可视化。支持本地 Ollama 和云端 LLM，数据 100% 本地存储，无需联网。

**核心功能：**
- 🕸️ 人物关系知识图谱（力导向网络图）
- 🗺️ 小说世界地图自动生成
- ⏳ 多泳道时间线/故事线视图
- 📖 小说百科全书（人物/地点/物品/组织/概念）
- 🤖 RAG 智能问答

---

## 2. 前端技术栈识别

### 2.1 框架与核心库

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 前端框架 | React | 19.2.0 | 核心 UI 框架 |
| 语言 | TypeScript | ~5.9.3 | 类型安全开发 |
| 构建工具 | Vite | 7.3.2 | 快速构建与热更新 |
| 样式框架 | Tailwind CSS | 4.1.18 | 原子化 CSS |
| UI 组件库 | shadcn/ui | 3.8.4 | 高质量组件库 |
| 状态管理 | Zustand | 5.0.11 | 轻量级状态管理 |
| 路由 | react-router-dom | 7.13.0 | 单页应用路由 |
| 桌面框架 | Tauri | 2.x | 跨平台桌面应用 |

### 2.2 可视化库

| 库名称 | 用途 |
|--------|------|
| D3.js (d3-drag, d3-selection, d3-shape, d3-transition, d3-zoom) | 地图渲染、力导向图 |
| react-force-graph-2d | 人物关系知识图谱可视化 |
| react-leaflet / leaflet | 地理地图展示 |
| maplibre-gl | 高性能地图渲染引擎 |
| roughjs | 手绘风格渲染 |

### 2.3 工具库

| 库名称 | 用途 |
|--------|------|
| lucide-react | 图标库 |
| clsx / tailwind-merge | CSS 类名处理 |
| pako | GZIP 压缩/解压缩 |
| react-markdown | Markdown 渲染 |

### 2.4 项目结构

```
frontend/src/
├── app/                    # 应用入口与路由
│   ├── App.tsx             # 根组件
│   ├── router.tsx          # 路由配置
│   ├── providers.tsx       # 全局 Provider
│   └── layouts/            # 布局组件
├── pages/                  # 页面组件
│   ├── GraphPage.tsx       # 关系图谱页
│   ├── MapPage.tsx         # 地图页
│   ├── TimelinePage.tsx    # 时间线页
│   ├── EncyclopediaPage.tsx # 百科页
│   ├── ReadingPage.tsx     # 阅读页
│   └── ChatPage.tsx        # 智能问答页
├── components/             # 通用组件
│   ├── visualization/      # 可视化组件
│   ├── entity-cards/       # 实体卡片组件
│   ├── shared/             # 共享组件
│   └── ui/                 # shadcn/ui 组件
├── stores/                 # Zustand 状态管理
│   ├── novelStore.ts       # 小说数据状态
│   ├── analysisStore.ts    # 分析任务状态
│   └── chatStore.ts        # 对话状态
├── providers/              # 数据提供者
│   ├── ApiDataProvider.ts  # API 数据提供者
│   └── DemoDataProvider.ts # 演示数据提供者
├── lib/                    # 工具函数与算法
│   ├── mapRenderer/        # 地图渲染引擎
│   ├── territoryGenerator.ts # 领地生成算法
│   └── coastlineGenerator.ts # 海岸线生成
└── api/                    # API 客户端
    ├── client.ts           # REST API 客户端
    └── types.ts            # TypeScript 类型定义
```

### 2.5 状态管理示例

```typescript
// src/stores/novelStore.ts
import { create } from "zustand"

interface NovelState {
  novels: Novel[]
  currentNovelId: string | null
  loading: boolean
  setCurrentNovelId: (id: string | null) => void
  fetchNovels: () => Promise<void>
}

export const useNovelStore = create<NovelState>((set) => ({
  novels: [],
  currentNovelId: null,
  loading: false,
  setCurrentNovelId: (id) => set({ currentNovelId: id }),
  fetchNovels: async () => {
    set({ loading: true })
    const data = await apiFetchNovels()
    set({ novels: data.novels, loading: false })
  },
}))
```

---

## 3. 后端技术栈识别

### 3.1 核心技术

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言 | Python | ≥3.9 | 后端开发语言 |
| Web 框架 | FastAPI | - | 异步 Web 框架 |
| 数据库 | SQLite | - | 结构化数据存储 |
| 向量数据库 | ChromaDB | - | RAG 向量检索 |
| ORM | Pydantic | ≥2.0 | 数据验证与模型 |
| 异步驱动 | aiosqlite | - | SQLite 异步操作 |

### 3.2 LLM 集成

| 类型 | 服务 | 说明 |
|------|------|------|
| 本地 LLM | Ollama | 支持 qwen3:8b 等模型 |
| 云端 LLM | OpenAI/Claude/Gemini | 10+ 供应商支持 |
| 中文分词 | jieba | 中文文本分词 |

### 3.3 项目结构

```
backend/src/
├── api/                    # API 层
│   ├── routes/             # REST API 路由
│   │   ├── analysis.py     # 分析任务接口
│   │   ├── chat.py         # 对话接口
│   │   ├── graph.py        # 图谱数据接口
│   │   ├── map.py          # 地图数据接口
│   │   └── novels.py       # 小说管理接口
│   ├── websocket/          # WebSocket 接口
│   │   ├── analysis_ws.py  # 分析进度推送
│   │   └── chat_ws.py      # 流式对话
│   ├── schemas/            # Pydantic 模型
│   └── main.py             # FastAPI 入口
├── db/                     # 数据层
│   ├── sqlite_db.py        # SQLite 连接管理
│   ├── novel_store.py      # 小说数据存储
│   ├── chapter_store.py    # 章节数据存储
│   └── world_structure_store.py # 世界结构存储
├── services/               # 业务服务层
│   ├── analysis_service.py # 分析服务
│   ├── geo_resolver.py     # 地理解析服务
│   ├── hierarchy_consolidator.py # 层级整合服务
│   ├── entity_aggregator.py # 实体聚合服务
│   └── export_service.py   # 导出服务
├── extraction/             # LLM 提取模块
│   ├── chapter_fact_extractor.py # 事实提取
│   ├── entity_pre_scanner.py     # 实体预扫描
│   ├── scene_llm_extractor.py    # 场景提取
│   └── prompts/            # 提示词模板
├── models/                 # 数据模型
│   ├── chapter_fact.py     # 章节事实模型
│   ├── entity_dict.py      # 实体词典模型
│   └── world_structure.py  # 世界结构模型
└── infra/                  # 基础设施
    ├── llm_client.py       # LLM 客户端抽象
    ├── openai_client.py    # OpenAI 客户端
    ├── anthropic_client.py # Claude 客户端
    └── config.py           # 配置管理
```

### 3.4 API 路由概览

```python
app = FastAPI(title="AI Reader V2", version="0.71.8", lifespan=lifespan)

# REST 路由
app.include_router(novels.router)        # /api/novels
app.include_router(chapters.router)      # /api/chapters
app.include_router(entities.router)      # /api/entities
app.include_router(graph.router)         # /api/graph
app.include_router(map.router)           # /api/map
app.include_router(timeline.router)      # /api/timeline
app.include_router(chat.router)          # /api/chat
app.include_router(analysis.router)      # /api/analysis
app.include_router(export_import.router) # /api/export-import

# WebSocket 路由
app.include_router(analysis_ws.router)   # /ws/analysis
app.include_router(chat_ws.router)       # /ws/chat
```

---

## 4. 整体架构梳理

### 4.1 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           前端层 (Frontend)                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────────────┐  │
│  │  Web Browser│  │  Tauri App  │  │   Demo Page │  │   Desktop App     │  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └─────────┬─────────┘  │
└─────────┼────────────────┼────────────────┼───────────────────┼─────────────┘
          │                │                │                   │
          ▼                ▼                ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API 网关层                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    FastAPI + WebSocket Server                       │   │
│  │  REST API: /api/novels, /api/chapters, /api/graph, /api/chat       │   │
│  │  WebSocket: /ws/analysis (进度推送), /ws/chat (流式对话)            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         ▼                            ▼                            ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   Services层    │       │  Extraction层   │       │    数据层        │
│ ┌─────────────┐ │       │ ┌─────────────┐ │       │ ┌─────────────┐ │
│ │analysis     │ │       │ │fact_extractor││       │ │ SQLite      │ │
│ │geo_resolver │ │       │ │entity_scanner││       │ │ ChromaDB    │ │
│ │entity_agg   │ │       │ │scene_extractor││       │ │ FileStorage │ │
│ │export       │ │       │ └─────────────┘ │       │ └─────────────┘ │
│ └─────────────┘ │       └─────────────────┘       └─────────────────┘
└─────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          LLM 层 (大语言模型)                               │
│  ┌─────────────────┐                    ┌─────────────────────────────┐   │
│  │   Ollama (本地) │                    │   Cloud LLM (云端)          │   │
│  │  qwen3:8b       │                    │  OpenAI/Claude/Gemini/...  │   │
│  │  无需联网       │                    │  需要 API Key               │   │
│  └─────────────────┘                    └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 数据流向

**分析流程：**
```
上传小说 → 章节切分 → 实体预扫描 → 逐章LLM提取 → 聚合处理 → 可视化
```

**核心数据流：**

| 阶段 | 数据格式 | 说明 |
|------|----------|------|
| 输入 | `.txt` / `.md` | 原始小说文本 |
| 章节切分 | JSON | 章节列表 |
| 实体预扫描 | JSON | 高频实体词典 |
| LLM 提取 | JSON | 提取的事实 |
| 聚合处理 | JSON | 去重归一化后的数据 |
| 可视化 | JSON | 图谱/地图/时间线数据 |

### 4.3 核心业务模块

| 模块 | 职责 | 关键技术 |
|------|------|----------|
| Analysis Service | 协调整个分析流程 | 异步任务管理 |
| Entity Extraction | 实体识别与分类 | jieba + LLM |
| Relation Extraction | 人物关系提取 | LLM 多轮提示 |
| Geo Resolution | 地点层级构建 | Edmonds 算法 |
| Timeline Builder | 时间线构建 | 事件聚合 |
| RAG Chat | 基于原文的问答 | ChromaDB |

---

## 5. 技术选型理由分析

### 5.1 前端技术选型

| 技术 | 选型理由 | 优势 | 挑战 |
|------|----------|------|------|
| React 19 | 生态成熟，社区活跃 | 组件复用、Hooks | 学习曲线较陡 |
| TypeScript | 类型安全 | 编译时检查 | 额外类型定义工作 |
| Vite | 新一代构建工具 | 极速冷启动 | 生态不如 Webpack |
| Tailwind CSS 4 | 原子化 CSS | 快速开发 | CSS 文件可能过大 |
| Zustand | 轻量级状态管理 | 简洁 API | 不适合复杂状态 |
| Tauri 2 | 跨平台桌面框架 | 小体积、安全 | Rust 学习成本 |

### 5.2 后端技术选型

| 技术 | 选型理由 | 优势 | 挑战 |
|------|----------|------|------|
| Python | AI/ML 生态成熟 | 丰富的 NLP 库 | 性能不如编译型语言 |
| FastAPI | 异步 Web 框架 | 高性能、自动文档 | 生态不如 Django |
| SQLite | 轻量级嵌入式数据库 | 零配置 | 并发写入有限 |
| ChromaDB | 轻量级向量数据库 | 易于部署 | 功能不如专业向量数据库 |
| Ollama | 本地 LLM 运行时 | 离线运行、隐私保护 | 硬件要求较高 |

---

## 6. 项目构建流程

### 6.1 环境要求

| 依赖 | 版本 |
|------|------|
| Python | ≥3.9 |
| Node.js | ≥22 |
| uv | latest |
| Ollama | latest (可选) |

### 6.2 开发环境搭建

```bash
# 启动 Ollama（可选）
ollama pull qwen3:8b && ollama serve

# 启动后端
cd backend && uv sync && uv run uvicorn src.api.main:app --reload

# 启动前端
cd frontend && npm install && npm run dev
```

### 6.3 构建命令

```bash
# 前端构建
npm run build                    # 生产构建
npm run build:demo               # Demo 构建
npm run build:desktop            # Tauri 桌面构建

# 后端打包
cd backend && uv run pyinstaller ai-reader-sidecar.spec
```

### 6.4 CI/CD 流程

项目使用 GitHub Actions 实现自动化构建和发布，包含：
- 代码检查
- 单元测试（500+ tests）
- 前端构建
- 桌面应用打包
- GitHub Release 发布

---

## 7. 关键技术亮点

### 7.1 地理层级构建算法

采用 Edmonds 最大权有向生成树算法，将层级构建建模为组合优化问题，130ms 内求解全局最优解。

### 7.2 实体别名归一化

Canonical 选名策略：3字全名优先、频率 fallback、绰号降权、防桥接机制。

### 7.3 地图渲染引擎

多层级渲染：地形合成、海岸线覆盖保证、rough.js 手绘风格渲染。

### 7.4 异步分析管线

支持异步执行、暂停恢复、失败重试、Token 预算自动缩放。

---

## 8. 总结

**AI Reader V2** 是一个技术栈完整、架构清晰的 AI 小说分析工具：

- **前端**：React 19 + TypeScript + Vite + Tailwind CSS 4
- **后端**：Python + FastAPI + SQLite + ChromaDB
- **桌面**：Tauri 2 框架
- **核心算法**：Edmonds 最大权生成树、实体别名归一化、地图地形合成

项目采用前后端分离架构，API 设计 RESTful，支持 WebSocket 实时推送，数据 100% 本地存储，兼顾隐私与性能。