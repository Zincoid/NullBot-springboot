# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash

- Java 21, Spring Boot 4.0.0, Maven. Compiler plugin is set to Java 19 source/target.
- Active config is `src/main/resources/application.yml`. Secrets are in that file — do not commit these publicly.

## Architecture Overview

**NullBot is a QQ chat bot** backed by the [Shiro](https://github.com/MisakaTAT/Shiro) framework (`com.mikuac:shiro:2.5.3`). Shiro connects to the QQ protocol via a WebSocket bridge (NapCat/Shamrock), receives events, and dispatches them to annotated handler methods. The bot also exposes a **REST API + STOMP WebSocket** layer for an admin frontend (`nullbot-frontend` Nginx container).

### Message Flow

```
QQ client → NapCat (WS) → Shiro framework → @Shiro-annotated methods
    → CommandProcessor → CommandHandlerChain → Command.execute()
```

1. **`CommandListener`** (`dispatcher/listener/`) — `@Shiro`-annotated, receives `GroupMessageEvent`, `PrivateMessageEvent`, `@At` pokes, etc. Messages starting with the `command.prefix` (`/`) are treated as commands; others may trigger AI chat or be ignored.
2. **`MonitorListener`** — hooks into the same events for passive behaviors: poke/reaction detection, message collection (for AI memory), keyword detection, image collection, auto-reply, etc. Each is individually toggleable via `@FunctionControl`.
3. **`CommandProcessor`** — looks up the command name in `CommandRegistry`, wraps it in a `CommandHandlerChain`.
4. **Handler chain** (ordered via `@Order`):
   - `RegisterHandler` (-1): auto-registers new groups/users in DB
   - `PermissionHandler` (0): checks access levels, command bans, user bans, maintenance mode
   - `RateLimitHandler` (2): Bucket4j-based per-group/per-user/per-command rate limiting
   - `ExecutorHandler` (4): dispatches to `Command.execute()` with appropriate event type

### Command System

- All commands implement the `Command` interface (`command/Command.java`). Each has `execute()` overloads for group messages, private messages, pokes, and recall notices.
- Commands are registered by annotating the class with `@CommandMapping({"Name", "别名"})`. `CommandRegistry` scans all `Command` beans from Spring context at startup and maps names → instances. There is no manual registration.
- Commands live under `command/` organized by category: `ai/`, `game/basic/`, `game/multi/`, `game/single/`, `assist/endfield/`, `convert/`, `image/`, `manage/`, `recall/`, `saying/`, `schedule/`, `system/`, `video/`.
- The `Chat` command (AI chat) is the default fallback for non-command messages in private chat and @-mentions.

### Dynamic Feature Toggle

`@FunctionControl(id = "FeatureName", enabled = true)` on a method or class enables runtime toggling. `FunctionAspect` intercepts annotated methods and checks `FunctionManager`, which holds an in-memory toggle map initialized from annotations. Toggles can be changed at runtime via `FuncSetCommand` or the admin API.

### Entity / DB Layer

- **PO** (`entity/po/`) — MyBatis-Plus persistent objects mapped to DB tables.
- **VO/DTO** — view objects and request DTOs for the REST API.
- **entity/page/** — page request wrappers for admin frontend queries.
- **entity/info/** — in-memory config containers (e.g., `SettingInfo`, `FileInfo`).
- Mappers under `mapper/`, with XML files in `resources/mapper/`. Uses MyBatis-Plus `BaseMapper` — most CRUD is via built-in methods, custom SQL only when needed.

### REST API + Admin Frontend

- Controllers under `controller/`: `LoginController`, `UserController`, `GroupController`, `SettingController`, `SystemController`, `FileController`, `OssController`, etc.
- JWT authentication: `JwtTool` uses a JKS keystore (`key/JwtSignKey.jks`), configured in `JwtProperties`. `WebInterceptor` validates JWT on all web requests.
- STOMP WebSocket at `/ws` for real-time log streaming to the admin panel. `WebSocketSender` broadcasts to subscribed clients.

### Game System

Multiplayer games (`game/multi/`) use a matchmaking pattern:
- `Matcher` queues players, `MatchPoolManager` holds pending pools per game type.
- When matched, a `GameMatchHandler` subclass (e.g., `TicTacToeMatchHandler`) creates the game state and processes moves.
- `GameLogic` subclasses (e.g., `TicTacToeGameLogic`) contain pure game rules.
- `MatchCleanupScheduler` periodically cleans expired matches.

### External Services

- **DeepSeek API** — AI chat/completion (`component/ai/DeepSeekClient.java`), configured in `nullbot.ai.deepseek.*`
- **TTS** — text-to-speech via external API (`component/ai/TtsClient.java`), configured in `nullbot.ai.tts.*`
- **Headless Chrome** — `Selenium` + `WebDriverManager` for HTML rendering and screenshots (`component/render/`)

### Key Config Properties

All under `application.yml` → `nullbot.*`:
- `bot-id` (QQ account), `admin-id`, `log-id` (admin group)
- `command.prefix` (default `/`)
- `file.storage.*` — paths for files, images, video, audio, temp, config, resources
- `ai.deepseek.*` — API URL, key, model params, default system prompt
- `chrome.*` — driver path, auto management, load timeout

### Docker Compose

Three services: `nullbot-backend` (this app), `nullbot-frontend` (Nginx), and optionally `nullbot-mysql` + `nullbot-napcat`. Shared network `nullbot-net`. Backend needs `shm_size: 2g` for Chrome.

### Deployment (from README)

- Bot process: `screen -dmS nullbot bash -c "java -jar /root/Nullbot/target/NullBot-springboot-0.0.1-SNAPSHOT.jar"`
- System watchdog: `syswatch.sh` monitors for crashes
- Chrome version: 144.0.7559.59 for local testing
