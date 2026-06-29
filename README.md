# NullBot

NullBot 是一个基于 Spring Boot 和 Shiro 框架的 QQ 群聊机器人，支持丰富的娱乐、管理与 AI 对话功能。后端提供 REST API + STOMP WebSocket 供管理前端使用。

## GitHub & Console

<div style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
  <a href="https://github.com/Zincoid/NullBot-springboot">
    <img src="https://img.shields.io/badge/View%20on%20GitHub-181717?logo=github&logoColor=white&style=for-the-badge" alt="GitHub">
  </a>
  <a href="http://nullbot.zincoid.online/">
    <img src="https://img.shields.io/badge/Console-0088CC?style=for-the-badge" alt="控制台">
  </a>
</div>

## 功能特性

- **AI 智能对话** -- 接入 DeepSeek API，支持多轮对话、长短期记忆、工具调用（表情包、语音、戳一戳等）
- **语音合成 (TTS)** -- 文本转语音，支持多音色
- **娱乐小游戏** -- 签到、抽卡、装备、签到、漂流瓶、猜图、 duel、养面包、娶群友等单人游戏；五子棋、黑白棋、战利品分配等多人匹配游戏
- **图片/视频管理** -- 群聊图片与视频的分类存储、随机展示
- **语录系统** -- 群聊精华语录的收藏与随机展示
- **定时提醒** -- 一次性/周期性群内提醒
- **权限管理** -- 多级权限控制、用户/命令封禁
- **速率限制** -- Bucket4j 实现的按群/按用户/按命令限流
- **动态功能开关** -- 运行时开关各项功能，无需重启
- **管理前端** -- 配套前端面板（独立 Nginx 容器），通过 JWT 认证的 REST API 管理机器人
- **日志流** -- STOMP WebSocket 实时推送日志到管理面板

## 技术栈

| 类别    | 技术                                                      |
|-------|---------------------------------------------------------|
| 语言    | Java 21                                                 |
| 框架    | Spring Boot 4.x                                         |
| QQ 协议 | Shiro (com.mikuac:shiro) + NapCat/Shamrock WebSocket 桥接 |
| ORM   | MyBatis-Plus                                            |
| 数据库   | MySQL                                                   |
| AI    | DeepSeek API (兼容 OpenAI API 格式)                         |
| 限流    | Bucket4j                                                |
| 渲染    | Selenium + Headless Chrome, resvg-jni                   |
| 认证    | JWT (jjwt)                                              |
| 前端通信  | STOMP WebSocket                                         |
| 构建    | Maven                                                   |
| 部署    | Docker Compose                                          |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Chrome / ChromeDriver (使用渲染功能时需要)

### 本地开发

1. 克隆仓库

    ```bash
    git clone https://github.com/Zincoid/NullBot-springboot.git
    cd NullBot-springboot
    ```

2. 配置数据库和机器人参数

    ```bash
    cp src/main/resources/application-prod.yml.template src/main/resources/application-prod.yml
    # 编辑 application-prod.yml 填写数据库连接、Shiro WebSocket 地址、AI API Key 等
    ```

3. 编译运行

    ```bash
    mvn clean package -DskipTests
    java -jar target/NullBot-springboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
    ```

### Docker 部署

项目提供完整 Docker Compose 编排：

```bash
# 构建后端镜像
docker build -f Dockerfile -t nullbot-backend .

# 启动
docker-compose up -d
```

默认服务：
- `nullbot-backend` -- Spring Boot 应用，端口 8080
- `nullbot-frontend` -- Nginx 管理前端，端口 80

基础镜像 `nullbot-jre-chrome:21` 需预先通过 `Dockerfile.base` 构建（包含 JRE + Chrome + ChromeDriver + 中文字体）。

## 项目结构

```
src/main/java/com/zincoid/nullbot/
├── bot/                        # QQ 机器人层
│   ├── command/                # 命令实现（按类别分组）
│   │   ├── aichat/             # AI 聊天命令
│   │   ├── assist/             # 辅助命令（帮助、精粹等）
│   │   ├── audio/              # 语音命令
│   │   ├── convert/            # 图片转换命令
│   │   ├── game/               # 游戏命令（basic/multi/single）
│   │   ├── image/              # 图片管理命令
│   │   ├── manage/             # 管理命令
│   │   ├── recall/             # 撤回通知
│   │   ├── saying/             # 语录命令
│   │   ├── schedule/           # 定时提醒命令
│   │   ├── system/             # 系统命令
│   │   └── video/              # 视频管理命令
│   ├── gateway/
│   │   ├── handler/            # 命令处理链（权限、限流、执行等）
│   │   ├── listener/           # Shiro 事件监听器
│   │   └── processor/          # 命令解析与分发
│   └── interceptor/            # 消息拦截器
├── core/                       # 业务逻辑与基础设施
│   ├── mapper/                 # MyBatis-Plus 数据映射
│   ├── model/                  # 数据模型（PO、DTO、VO）
│   ├── module/
│   │   ├── ai/                 # AI 聊天引擎与 TTS
│   │   ├── control/            # 功能控制与限流
│   │   ├── game/               # 游戏引擎（匹配、逻辑、状态管理）
│   │   ├── render/             # 渲染引擎（Chrome、resvg）
│   │   ├── resource/           # 资源管理
│   │   ├── security/           # JWT 安全
│   │   └── storage/            # 文件存储
│   ├── properties/             # 配置属性类
│   ├── service/                # 服务层
│   └── utils/                  # 工具类
├── web/                        # REST API 层
│   └── controller/             # 控制器（登录、用户、群组、文件、设置等）
└── websocket/                  # STOMP WebSocket 日志推送
```

## 命令系统

消息以 `command.prefix`（默认为 `/`）开头时触发命令处理，处理流程：

```
QQ 消息 -> Shiro 监听器 -> 命令处理器 -> 处理链（注册 -> 权限 -> 限流 -> 执行）
```

命令通过 `@CommandMapping` 注解注册，支持中英文别名，启动时自动扫描。

## 配置说明

主要配置项（`application.yml`）：

| 配置                               | 说明                   |
|----------------------------------|----------------------|
| `nullbot.shiro.url`              | Shiro WebSocket 连接地址 |
| `nullbot.basic.ids.bot`          | 机器人 QQ 号             |
| `nullbot.basic.ids.admin`        | 管理员 QQ 号             |
| `nullbot.basic.cmd.prefix`       | 命令前缀（默认 /）           |
| `nullbot.ai.chat.openai.api-url` | AI API 地址            |
| `nullbot.ai.chat.openai.api-key` | AI API 密钥            |
| `nullbot.ai.chat.default-prompt` | AI 默认人格提示词           |
| `nullbot.file.storage.root`      | 文件存储根路径              |
| `nullbot.web.jwt.*`              | JWT 密钥与有效期配置         |

## License

[MIT](LICENSE)
