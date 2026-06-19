$content = @'
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
| D3.js | 地图渲染、力导向图 |
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
├── pages/                  # 页面组件
├── components/             # 通用组件
├── stores/                 # Zustand 状态管理
├── providers/              # 数据提供者
├── lib/                    # 工具函数与算法
└── api/                    # API 客户端
```

---

## 3. 后端技术栈识别

### 3.1 核心技术

| 分类 | 技术 | 用途 |
|------|------|------|
| 语言 | Python | 后端开发语言 |
| Web 框架 | FastAPI | 异步 Web 框架 |
| 数据库 | SQLite | 结构化数据存储 |
| 向量数据库 | ChromaDB | RAG 向量检索 |
| ORM | Pydantic | 数据验证与模型 |

### 3.2 LLM 集成

| 类型 | 服务 |
|------|------|
| 本地 LLM | Ollama |
| 云端 LLM | OpenAI/Claude/Gemini |
| 中文分词 | jieba |

### 3.3 项目结构

```
backend/src/
├── api/                    # API 层
├── db/                     # 数据层
├── services/               # 业务服务层
├── extraction/             # LLM 提取模块
├── models/                 # 数据模型
└── infra/                  # 基础设施
```

---

## 4. 整体架构梳理

### 4.1 架构图

前端层 -> API网关层 -> Services层 -> LLM层

### 4.2 数据流向

上传小说 -> 章节切分 -> 实体预扫描 -> 逐章LLM提取 -> 聚合处理 -> 可视化

### 4.3 核心业务模块

| 模块 | 职责 |
|------|------|
| Analysis Service | 协调整个分析流程 |
| Entity Extraction | 实体识别与分类 |
| Relation Extraction | 人物关系提取 |
| Geo Resolution | 地点层级构建 |
| Timeline Builder | 时间线构建 |
| RAG Chat | 基于原文的问答 |

---

## 5. 技术选型理由分析

### 5.1 前端技术选型

| 技术 | 选型理由 |
|------|----------|
| React 19 | 生态成熟，社区活跃 |
| TypeScript | 类型安全 |
| Vite | 新一代构建工具 |
| Tailwind CSS 4 | 原子化 CSS |
| Zustand | 轻量级状态管理 |
| Tauri 2 | 跨平台桌面框架 |

### 5.2 后端技术选型

| 技术 | 选型理由 |
|------|----------|
| Python | AI/ML 生态成熟 |
| FastAPI | 异步 Web 框架 |
| SQLite | 轻量级嵌入式数据库 |
| ChromaDB | 轻量级向量数据库 |
| Ollama | 本地 LLM 运行时 |

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
# 启动后端
cd backend && uv sync && uv run uvicorn src.api.main:app --reload

# 启动前端
cd frontend && npm install && npm run dev
```

### 6.3 构建命令

```bash
npm run build                    # 生产构建
npm run build:desktop            # Tauri 桌面构建
```

### 6.4 CI/CD 流程

项目使用 GitHub Actions 实现自动化构建和发布。

---

## 7. 关键技术亮点

- 地理层级构建：Edmonds 最大权有向生成树算法
- 实体别名归一化：Canonical 选名策略
- 地图渲染引擎：多层级渲染
- 异步分析管线：支持异步执行、暂停恢复

---

## 8. 总结

**AI Reader V2** 是一个技术栈完整、架构清晰的 AI 小说分析工具。
'@

Set-Content -Path "TECHNICAL_ANALYSIS.md" -Value $content -Encoding UTF8
Write-Host "Report generated successfully!"
