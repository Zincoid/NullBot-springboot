# Task 1 Report: WebCtx.requireAdmin + WebInterceptor 重做 + AuthController 路由

## 状态: DONE

## 改动叙述

### 1. `src/main/java/com/zincoid/nullbot/core/context/WebCtx.java`

- 新增 import `com.zincoid.nullbot.core.exception.CoreException`。
- 在 `getType()` 之后新增 `requireAdmin()` 静态方法(threads 2+,供 Task 2~6 引用):

```java
public static void requireAdmin() {
    if (getType() == null)
        throw new CoreException("未登录");
    if (getType() != 1)
        throw new CoreException("访客受限");
}
```

### 2. `src/main/java/com/zincoid/nullbot/web/interceptor/WebInterceptor.java`

- 整体删除 `GUEST_FORBIDDEN_URLS` 静态字段、静态初始化块,以及 preHandle 中 `userType == 0` 的受限 URL 循环。
- 删除孤儿 import:`java.util.Arrays`、`java.util.List`。
- 放行判断改为三条资源路由:
    - `uri.equals("/nullbot/auth/login") || uri.equals("/nullbot/auth/guest")` → "登录放行"
    - `uri.equals("/nullbot/auth/regist")` → "注册放行"
    - `uri.startsWith("/nullbot/oss")` → 保留不变
- `userType == 0` 分支改为直接 `log.info("└─[WebInterceptor] 访客放行"); return true;`(访客限制改由各 Controller 方法首行的 `WebCtx.requireAdmin()` 承担)。
- `userType == 1` 管理放行、JWT 解析、`WebCtx.set/remove` 等其余逻辑零改动。

### 3. `src/main/java/com/zincoid/nullbot/web/controller/AuthController.java`

- 类级注解改为 `@RequestMapping("/nullbot/auth")`。
- 三个公开端点完全不动(仅挂在新类级前缀下隐式变为 `/nullbot/auth/*`):
    - `@PostMapping("/regist")`、`@PostMapping("/guest")`、`@PostMapping("/login")`
- 写读端点重映射(方法体除首行加锁外零改动):
    - `@GetMapping("/me")` — 原 GET `/nullbot/info`,body 未动。
    - `@PutMapping("/me")` — 原 POST `/update`,首行加 `WebCtx.requireAdmin();`,后续 `admin.setId(WebCtx.getId())` 等原样保留。
    - `@DeleteMapping("/me")` — 原 DELETE `/nullbot/delete`,首行加 `WebCtx.requireAdmin();`。
    - `@PutMapping("/me/password")` — 原 POST `/nullbot/password`,首行加 `WebCtx.requireAdmin();`。
- 中文日志文案全部保持原样。

## 编译结果

```
mvn clean compile -q -o -DskipTests
EXIT=0
```

一次通过,exit 0,无告警输出。

## 提交

- commit: `523c654f1f637ac814d19958990d67f49bd3e2b0`
- message: `refactor(web): requireAdmin 鉴权与 auth 路由`
- 仅包含 3 个目标文件(3 files changed, 19 insertions(+), 48 deletions(-))。

## 对后续任务的接口约定

- `WebCtx.requireAdmin() : void` — type 为 null 抛 "未登录",非 1 抛 "访客受限"。
- 放行白名单:`/nullbot/auth/login|regist|guest`、`/nullbot/oss*`。

## 疑虑

无。Task brief 中示例代码块的重复行(period 52–67 的重复 @Put/DELETE 片段)经与补充指令核对后按补充指令的单份语义实现。
