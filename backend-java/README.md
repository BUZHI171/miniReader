# AI Reader V2 Backend - Java版本

## 项目简介

这是 **AI Reader V2** 后端的Java版本实现，将原有的Python/FastAPI后端迁移到Java/Spring Boot。

## 技术栈

- **Java 17+** - 编程语言
- **Spring Boot 3.2.0** - Web框架
- **Spring Data JPA** - 数据访问
- **SQLite** - 嵌入式数据库
- **WebSocket** - 实时通信
- **Lombok** - 简化代码
- **Maven** - 项目构建

## 项目结构

```
backend-java/
├── src/
│   ├── main/
│   │   ├── java/com/aireader/v2/
│   │   │   ├── AiReaderApplication.java    # 主应用类
│   │   │   ├── config/                      # 配置类
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   └── LlmConfig.java
│   │   │   ├── controller/                  # REST控制器
│   │   │   │   └── NovelController.java
│   │   │   ├── service/                     # 业务服务层
│   │   │   │   └── NovelService.java
│   │   │   ├── repository/                  # 数据访问层
│   │   │   │   ├── NovelRepository.java
│   │   │   │   ├── ChapterRepository.java
│   │   │   │   └── ...
│   │   │   ├── model/                       # 数据模型
│   │   │   │   ├── entity/                  # JPA实体
│   │   │   │   │   ├── Novel.java
│   │   │   │   │   ├── Chapter.java
│   │   │   │   │   └── ...
│   │   │   │   └── enums/                  # 枚举类型
│   │   │   │       ├── LayerType.java
│   │   │   │       └── ...
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   │   ├── NovelDTO.java
│   │   │   │   └── ...
│   │   │   ├── websocket/                  # WebSocket处理
│   │   │   │   ├── AnalysisWebSocketHandler.java
│   │   │   │   └── ChatWebSocketHandler.java
│   │   │   ├── llm/                        # LLM集成
│   │   │   │   └── OllamaClient.java
│   │   │   ├── extraction/                 # 事实提取模块
│   │   │   ├── geo/                        # 地理处理模块
│   │   │   ├── exception/                  # 异常处理
│   │   │   └── util/                       # 工具类
│   │   └── resources/
│   │       └── application.properties      # 应用配置
│   └── test/                                # 测试代码
├── pom.xml                                 # Maven配置
└── README.md                               # 项目文档
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+

### 构建项目

```bash
cd backend-java
mvn clean install
```

### 运行应用

```bash
mvn spring-boot:run
```

服务将在 http://localhost:8000 启动。

### 运行测试

```bash
mvn test
```

## 核心功能

### 1. 小说管理
- 上传TXT/Markdown小说文件
- 智能章节切分
- 小说列表和详情查看
- 删除小说

### 2. 章节分析
- 基于LLM的章节事实提取
- 异步分析任务处理
- 实时进度推送（WebSocket）
- 暂停/恢复/取消分析

### 3. 知识图谱
- 人物关系提取
- 实体别名归一化
- 关系类型分类

### 4. 世界地图
- 地点层级构建
- Edmonds最大权生成树算法
- 空间关系补全

## API接口

### 小说接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/novels | 获取小说列表 |
| POST | /api/novels/upload | 上传小说文件 |
| GET | /api/novels/{id} | 获取小说详情 |
| DELETE | /api/novels/{id} | 删除小说 |
| GET | /api/novels/{id}/stats | 获取统计信息 |

### WebSocket接口

| 路径 | 说明 |
|------|------|
| /ws/analysis | 分析进度推送 |
| /ws/chat | 流式对话 |

## 配置说明

主要配置项（application.properties）：

```properties
# 服务器配置
server.port=8000

# 数据库配置
spring.datasource.url=jdbc:sqlite:./data/ai_reader.db

# LLM配置
aireader.llm.provider=ollama
aireader.llm.model=qwen3:8b
aireader.ollama.base-url=http://localhost:11434
```

## 待完成功能

1. **完整的数据模型迁移**
   - 人物、地点、组织、物品等实体
   - 关系数据模型
   - 空间关系模型

2. **API端点完整实现**
   - 分析API
   - 图谱API
   - 地图API
   - 时间线API
   - 导出API

3. **LLM集成完善**
   - OpenAI客户端
   - Anthropic客户端
   - 多模型支持

4. **核心算法实现**
   - Edmonds最大权生成树
   - 事实验证器
   - 别名解析器
   - 场景提取器

5. **单元测试**
   - Service层测试
   - Controller层测试
   - 集成测试

## 迁移对照表

### Python → Java

| Python | Java |
|--------|------|
| FastAPI | Spring Boot |
| Pydantic | JPA Entity + Lombok |
| aiosqlite | Spring Data JPA + SQLite |
| WebSocket | Spring WebSocket |
| asyncio | @Async + CompletableFuture |

## 许可证

与原项目一致：AGPL-3.0

## 贡献指南

欢迎提交Issue和Pull Request！
