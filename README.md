<div align="center">

<h1 align="center">TFind</h1>

<p align="center">
  <b>智慧厕所发现平台 — AI 驱动的厕所查找与管理助手</b><br/>
  通过地图导航、AI 智能评分、用户评价，帮你快速找到最合适的厕所
</p>

<p align="center">
  <a href="#快速开始">快速开始</a> ·
  <a href="#核心能力">核心能力</a> ·
  <a href="#系统架构">系统架构</a> ·
  <a href="#web端手机端与电脑端差异">Web 端差异</a> ·
  <a href="#项目结构">项目结构</a> ·
  <a href=https://gitcode.com/vinforlucky/TFind>GitCode</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/java-1.8-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="java">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="spring-boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2021.0.8-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="spring-cloud">
  <img src="https://img.shields.io/badge/WeChat%20MiniProgram-原生-green?style=flat-square&logo=wechat&logoColor=white" alt="wechat">
  <img src="https://img.shields.io/badge/AI-DashScope/Qwen-5A4FCF?style=flat-square&logo=alibabacloud&logoColor=white" alt="ai">
  <img src="https://img.shields.io/badge/gRPC-1.58.0-4285F4?style=flat-square&logo=google&logoColor=white" alt="grpc">

</p>

</div>

<br/>

> [!NOTE]
> **TFind 是你的智能厕所导航助手。**
> 它能帮你发现附近厕所、查看 AI 卫生评分、阅读用户评价、在地图上导航 — 无论是微信小程序还是网页端，都能一键找到最佳选择。

> [!TIP]
> TFind 集成阿里云 DashScope (Qwen) 大模型，通过 AI 分析厕所图片与环境，提供智能评分与推荐。数据本地存储，无需云端同步。

<br/>

---

## 目录

- [快速开始](#快速开始)
- [TFind 是什么](#tfind-是什么)
- [核心能力](#核心能力)
- [系统架构](#系统架构)
- [Web端手机端与电脑端差异](#web端手机端与电脑端差异)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [设计哲学](#设计哲学)
- [致谢](#致谢)
- [许可证](#许可证)

---

## 快速开始

### 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 1.8+ | Java 运行时 |
| Maven | 3.6+ | 项目构建与管理 |
| Redis | 6.0+ | 缓存与会话管理 |
| Node.js | 14+（可选） | 小程序开发工具 |
| OS | **Windows**（主要）/ macOS & Linux | |

### 1. 克隆项目

```bash
git clone https://github.com/your-username/TFind.git
cd TFind
```

### 2. 配置环境

#### 2.1 配置 Redis

确保本地 Redis 服务已启动（默认 `localhost:6379`）。

#### 2.2 配置 AI API（可选）

编辑 `tfind-parent/tfind-toilet-service/.env` 文件（可参考 `.env.example`）：

```bash
# 阿里云 DashScope API Key
DASHSCOPE_API_KEY=sk-your-api-key-here

# 模型选择
DASHSCOPE_MODEL=qwen-plus
DASHSCOPE_VISION_MODEL=qwen-vl-plus
```

> 获取 DashScope API Key: https://dashscope.console.aliyun.com/apiKey

### 3. 构建与启动

```bash
# 编译全部模块
cd tfind-parent
mvn clean package -DskipTests

# 按顺序启动服务（每个在新终端窗口中运行）
# 1. 用户服务 (端口 8081)
mvn spring-boot:run -pl tfind-user-service

# 2. 厕所服务 (端口 8082)
mvn spring-boot:run -pl tfind-toilet-service

# 3. 网关服务 (端口 8080)
mvn spring-boot:run -pl tfind-gateway

# 4. Web 管理端 (端口 8083)
mvn spring-boot:run -pl tfind-web
```

或使用便捷脚本（支持一键启动/停止）：

```bash
# 一键启动所有服务
start-all.bat

# 停止所有服务
stop-all.bat
```

### 4. 访问服务

| 服务 | 地址 | 说明 |
|---|---|---|
| **网关** | `http://localhost:8080` | 统一 API 入口 |
| **Web 管理端** | `http://localhost:8083` | 管理员后台（Thymeleaf） |
| **微信小程序** | 使用微信开发者工具打开 `tfind-miniprogram/` | 用户端 |

### 5. 微信小程序配置

1. 下载并打开 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入项目，选择 `tfind-miniprogram/` 目录
3. 在 `app.js` 中修改 `baseUrl` 为你的后端地址
4. 预览或真机调试

---

## TFind 是什么

TFind (Toilet Finder) 是一个基于微服务架构的智慧厕所发现平台，围绕 **四个核心场景** 展开：

```
🔍 查找厕所 → 📊 AI分析 → ⭐ 用户评价 → 🗺️ 地图导航
      ↑                                      │
      └────────── 智能评分与推荐闭环 ←─────────┘
```

与普通厕所查询工具不同，TFind 具备：

- **AI 智能评分**：通过通义千问视觉模型分析厕所图片，评估卫生状况
- **多模式推荐**：最快到达 / 最干净 / 智能综合推荐三种模式
- **众包数据**：用户上传厕所信息与评价，社区共建
- **审核机制**：管理员审核厕所信息，保证数据质量
- **多端适配**：一套后端，同时支持微信小程序、Web 手机端和电脑端

---

## 核心能力

### 🔍 附近厕所发现

- 基于地理位置搜索附近厕所
- 支持距离筛选（1-20公里范围可调）
- 显示厕所名称、地址、楼层、蹲位数量
- 高德地图定位与导航
- 三种推荐模式：最快到达 / 最干净 / 智能推荐

### 🤖 AI 智能分析

- 集成阿里云 DashScope (通义千问 Qwen) 大模型
- 视觉模型分析厕所环境图片，自动评分
- AI 评估维度：卫生状况、设施完整度、环境舒适度
- 智能综合推荐算法

### ⭐ 用户评价系统

- 厕所卫生评分（1-5分）
- 详细文字评论
- 评论更新与补充
- 评分统计与展示

### 📝 用户上传与举报

- 用户提交新厕所信息（名称、地址、照片、标签）
- 图片上传支持
- 厕所问题举报
- 管理员审核流程

### 🗺️ 地图导航

- 高德地图瓦片展示
- 厕所标记点（按评分分色显示）
- 搜索过滤功能
- 定位与导航

### 🛡️ 管理后台

- 仪表盘数据概览（用户数/厕所数/待审核数）
- 用户管理（添加/删除管理员、软删除）
- 厕所管理（审核通过/拒绝、软删除）
- 删除管理（恢复/物理删除）
- JWT 鉴权与会话管理

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户界面层                               │
│                                                                  │
│  微信小程序     │   Web 管理端 (手机版)  │  Web 管理端 (电脑版)  │
│  (原生WXML)     │   (Thymeleaf Mobile)   │  (Thymeleaf Desktop) │
└─────────────────────┬────────────────────────────────────────────┘
                      │  HTTP / gRPC
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│               Spring Cloud Gateway (8080)                        │
│              路由分发 · JWT 鉴权 · 限流                          │
└──────┬──────────────────────────────────────────────┬───────────┘
       │                                              │
       ▼                                              ▼
┌──────────────────────┐               ┌──────────────────────────┐
│  User Service (8081) │               │ Toilet Service (8082)    │
│                      │               │                          │
│  · 用户注册/登录     │               │  · 厕所 CRUD             │
│  · 用户管理         │               │  · AI 智能分析            │
│  · JWT 生成         │◄────gRPC─────│  · 评论 & 评分            │
│  · SQLite 存储      │               │  · 举报 & 审核            │
│                      │               │  · 用户上传管理          │
└──────────────────────┘               │  · BerkeleyDB 存储       │
                                       │  · FolkMQ 消息队列       │
                                       │  · Redis 缓存            │
                                       └──────────────────────────┘
                                                   │
                                                   ▼
                                       ┌──────────────────────────┐
                                       │  AI 服务 (DashScope)     │
                                       │  · Qwen 视觉模型         │
                                       │  · 图片分析评分          │
                                       │  · 智能推荐算法          │
                                       └──────────────────────────┘
```

**端口分配**：

| 服务 | 端口 | 技术栈 |
|---|---|---|
| Gateway | 8080 | Spring Cloud Gateway |
| User Service | 8081 | Spring Boot + SQLite |
| Toilet Service | 8082 | Spring Boot + BerkeleyDB + Redis |
| Web Admin | 8083 | Spring Boot + Thymeleaf |
| gRPC (Toilet) | 9092 | gRPC |
| gRPC (User) | 9091 | gRPC |

---

## Web端手机端与电脑端差异

> TFind Web 管理端通过 **User-Agent 自动检测** 区分手机端和电脑端访问，动态切换不同的模板和样式。
> 检测逻辑由 `MobileDetectionFilter` 和 `MobileInterceptor` 实现，
> 核心代码见 [MobileInterceptor.java](file:///d:/2Git/TFind/tfind-parent/tfind-web/src/main/java/com/tfind/web/interceptor/MobileInterceptor.java#L13-L33)。

### 检测机制

```
用户请求 → MobileDetectionFilter → 解析 User-Agent
          ├─ 包含 mobile/android/iphone 等 → isMobile=true
          └─ 否则 → isMobile=false
                    ↓
           Controller 根据 isMobile 属性
           返回对应模板路径
```

### 差异对比表

| 差异维度 | 🖥️ 电脑端 (Desktop) | 📱 手机端 (Mobile) |
|---|---|---|
| **布局方式** | 左侧固定侧边栏 (240px) + 右侧内容区 | 顶部蓝色标题栏 + 卡片列表 + 底部导航栏 |
| **导航方式** | 左侧侧边栏菜单，点击切换页面 | 底部固定 TabBar（首页/用户/厕所/退出），图标+文字 |
| **表格/列表** | 完整数据表格 `<table>`，包含表头和所有列 | 卡片式列表，每项一张卡片，信息精简 |
| **CSS 样式** | 外部 `admin.css` 样式表（[admin.css](file:///d:/2Git/TFind/tfind-parent/tfind-web/src/main/resources/static/css/admin.css)） | 内联 `<style>` 标签，各页面独立样式 |
| **JavaScript** | 外部 `admin.js` 脚本（[admin.js](file:///d:/2Git/TFind/tfind-parent/tfind-web/src/main/resources/static/js/admin.js)） | 内联 `<script>` 标签，使用 `fetch` API |
| **配色主题** | 紫色渐变 `#667eea → #764ba2` | 蓝色 `#4A90D9` |
| **Dashboard** | 大卡片网格布局，带悬浮动画 | 用户头像 + 三栏统计 + 菜单列表 |
| **地图页面** | Leaflet 地图 + 顶部悬浮搜索栏 + 左下角图例 | 返回微信小程序端（走小程序地图页） |
| **登录页面** | 居中卡片 400px 宽，紫色渐变背景 | 居中卡片 90% 宽度（max 360px），蓝色背景 |
| **删除管理** | Tab 切换（已删除用户/已删除厕所） | 分区标题 + 滚动列表 |
| **Viewport** | `width=device-width, initial-scale=1.0` | `maximum-scale=1.0, user-scalable=no`（禁止缩放） |
| **模板路径** | `templates/admin/`、`templates/login/`、`templates/map/` | `templates/mobile/` |
| **回退按钮** | 无（侧边栏始终可见） | 顶部标题栏左侧 `<` 返回按钮 |

### 模板映射关系

| 功能 | 电脑端模板 | 手机端模板 |
|---|---|---|
| 仪表盘 | [admin/dashboard.html] | [mobile/dashboard.html] |
| 用户管理 | [admin/users.html] | [mobile/users.html] |
| 厕所管理 | [admin/toilets.html] | [mobile/toilets.html] |
| 审核管理 | [admin/pending.html] | [mobile/pending.html] |
| 删除管理 | [admin/deleted.html] | [mobile/deleted.html] |
| 登录页面 | [login/login.html] | [mobile/login.html] |

### 微信小程序 vs Web 管理端

> 除 Web 手机版管理端外，TFind 还提供**微信小程序**作为主要用户端：

| 维度 | 微信小程序 | Web 管理端 |
|---|---|---|
| **用户群体** | 普通用户 | 管理员 |
| **核心功能** | 找厕所、AI推荐、评价、上传 | 用户管理、厕所审核、数据统计 |
| **地图** | 微信原生地图组件 | Leaflet + 高德瓦片 |
| **定位** | `wx.getLocation` 原生 API | / |
| **登录** | 微信 OpenID 登录 | 账号密码登录 |
| **底部导航** | 首页/地图/上传/我的 | 首页/用户/厕所/退出 |

---

## 项目结构

```
TFind/
├── tfind-miniprogram/                  # 微信小程序（用户端）
│   ├── app.js                          #   小程序入口，全局配置
│   ├── app.json                        #   页面路由、TabBar 配置
│   ├── app.wxss                        #   全局样式
│   ├── images/                         #   图标资源
│   └── pages/                          #   页面目录
│       ├── index/                      #     首页（附近厕所搜索）
│       ├── map/                        #     地图导航
│       ├── upload/                     #     上传厕所
│       ├── mine/                       #     个人中心
│       ├── toilet-detail/             #     厕所详情与评价
│       ├── recommend/                  #     AI 智能推荐
│       ├── login/                      #     登录
│       ├── register/                   #     注册
│       ├── change-password/           #     修改密码
│       └── admin/                      #     管理员后台
│
├── tfind-parent/                       # Maven 父项目
│   ├── pom.xml                         #   依赖与模块管理
│   │
│   ├── tfind-common/                   # 公共模块
│   │   └── src/main/java/com/tfind/common/
│   │       ├── config/                 #   Jackson 配置
│   │       ├── constant/               #   常量定义
│   │       ├── exception/              #   全局异常处理
│   │       ├── model/                  #   公共实体（ToiletInfo, UserInfo）
│   │       ├── result/                 #   统一返回结果 Result<T>
│   │       └── util/                   #   JwtUtil, SM3Util
│   │
│   ├── tfind-gateway/                  # API 网关 (端口 8080)
│   │   └── src/main/java/com/tfind/gateway/
│   │       ├── GatewayApplication.java #   启动类
│   │       ├── config/                 #   CORS, Security 配置
│   │       └── filter/                 #   JwtAuthFilter, RequestLogFilter
│   │
│   ├── tfind-user-service/             # 用户服务 (端口 8081)
│   │   └── src/main/java/com/tfind/user/
│   │       └── UserServiceApplication.java
│   │
│   ├── tfind-toilet-service/           # 厕所服务 (端口 8082)
│   │   ├── src/main/java/com/tfind/toilet/
│   │   │   ├── ToiletServiceApplication.java
│   │   │   ├── ai/                     #   AI 服务（DashScopeClient, AIService）
│   │   │   ├── config/                 #   BerkeleyDB, Redis, FolkMQ, JWT 配置
│   │   │   ├── controller/            #   Toilet, Review, Report, UserProfile
│   │   │   ├── entity/                 #   实体类
│   │   │   ├── grpc/                   #   gRPC 服务端
│   │   │   ├── mq/                     #   FolkMQ 消息队列
│   │   │   ├── scheduler/             #   定时任务（软删除清理）
│   │   │   └── service/               #   业务逻辑
│   │   ├── src/main/proto/            #   Protobuf 定义
│   │   └── data/                       #   BerkeleyDB 数据
│   │
│   └── tfind-web/                      # Web 管理端 (端口 8083)
│       └── src/main/java/com/tfind/web/
│           ├── WebApplication.java     #   启动类
│           ├── config/                 #   SecurityConfig, WebConfig
│           ├── controller/            #   AdminController, MapController,
│           │                          #   LoginController, ProfileController
│           ├── service/               #   ApiService（调用后端 API）
│           ├── interceptor/           #   MobileInterceptor（设备检测）
│           ├── filter/                #   MobileDetectionFilter
│           └── resources/
│               ├── templates/          #   Thymeleaf 模板
│               │   ├── admin/          #     电脑端管理模板
│               │   ├── mobile/         #     手机端管理模板
│               │   ├── login/          #     电脑端登录模板
│               │   ├── map/            #     地图模板
│               │   └── profile/        #     修改密码模板
│               └── static/             #   静态资源
│                   ├── css/admin.css   #
│                   └── js/admin.js     #
│
├── start-all.bat                       # 一键启动脚本
├── stop-all.bat                        # 一键停止脚本
├── .gitignore                          # Git 忽略规则
└── LICENSE                             # Apache 2.0 许可证
```

**技术栈**：

| 组件 | 技术 |
|---|---|
| 微服务框架 | Spring Boot 2.7 + Spring Cloud 2021.0.8 |
| API 网关 | Spring Cloud Gateway |
| 服务通信 | REST (HTTP) + gRPC |
| 用户端 | 微信小程序原生开发 |
| Web 管理端 | Spring Boot + Thymeleaf（手机/电脑自适应） |
| 数据库 | SQLite（用户服务）+ BerkeleyDB（厕所服务） |
| 缓存 | Redis |
| 消息队列 | FolkMQ |
| AI 模型 | 阿里云 DashScope / 通义千问 Qwen |
| 地图 | 高德地图瓦片 + Leaflet（Web）/ 微信原生地图（小程序） |
| 认证 | JWT + Spring Security |

---

## 设计哲学

1. **多端自适应** — Web 管理端自动识别设备类型，手机/电脑分别呈现优化界面
2. **AI 赋能** — 通过视觉大模型智能分析厕所环境，提供客观评分
3. **微服务解耦** — 用户服务、厕所服务独立部署，通过网关统一入口
4. **众包共建** — 用户可上传厕所信息与评价，社区共同维护数据质量
5. **审核把关** — 管理员审核机制确保数据真实可靠
6. **软删除设计** — 数据删除采用软删除，支持恢复与物理删除双模式

---

## 致谢

- **DashScope / 通义千问 (Qwen)** — 强大的 AI 视觉模型，驱动智能评分引擎
- **Spring Boot & Spring Cloud** — 优秀的 Java 微服务框架
- **微信小程序** — 便捷的用户端平台
- **Leaflet** — 开源地图库，支持高德瓦片
- **gRPC** — 高性能服务间通信
- **BerkeleyDB** — 嵌入式键值数据库
- **FolkMQ** — 轻量级消息队列

---
## Star History

<a href="https://www.star-history.com/?repos=vinlucky%2FLVV%2Cvinlucky%2FTFind&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=vinlucky/LVV%2Cvinlucky/TFind&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=vinlucky/LVV%2Cvinlucky/TFind&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=vinlucky/LVV%2Cvinlucky/TFind&type=date&legend=top-left" />
 </picture>
</a>
---

## 许可证

本项目采用 [Apache 2.0](LICENSE) 开源协议。

---

<p align="center">
  <sub>
    Made with ❤️ for everyone who needs to find a toilet faster.<br/>
    如果 TFind 帮到了你，考虑给一个 Star ⭐ 项目不足之处请见谅
  </sub>
</p>
