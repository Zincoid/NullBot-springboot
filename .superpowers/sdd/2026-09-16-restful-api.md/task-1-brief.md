### Task 1: WebCtx.requireAdmin + WebInterceptor 重做 + AuthController 路由

**Files:**
- Modify: `core/context/WebCtx.java`
- Modify: `web/interceptor/WebInterceptor.java`
- Modify: `web/controller/AuthController.java`

**Interfaces:**
- Produces: `WebCtx.requireAdmin() : void`(threads 2+)被 Task 2~6 引用
- Produces: 新放行路径 `/nullbot/auth/login|regist|guest`、`/nullbot/oss` 保留

- [ ] **Step 1: WebCtx 加 requireAdmin**

`WebCtx` 加 import `com.zincoid.nullbot.core.exception.CoreException`,并在 `getType()` 后追加:

```java
    public static void requireAdmin() {
        if (getType() == null)
            throw new CoreException("未登录");
        if (getType() != 1)
            throw new CoreException("访客受限");
    }
```

- [ ] **Step 2: WebInterceptor 删除访客黑名单、放行路径改 auth**

`GUEST_FORBIDDEN_URLS` 静态块与 `userType == 0` 的受限循环整个删除;放行判断改为:

```java
        if (uri.equals("/nullbot/auth/login") || uri.equals("/nullbot/auth/guest")) {
            log.info("└─[WebInterceptor] 登录放行");
            return true;
        }
        if (uri.equals("/nullbot/auth/regist")) {
            log.info("└─[WebInterceptor] 注册放行");
            return true;
        }
```

`userType==0` 分支改为直接 `log.info("└─[WebInterceptor] 访客放行"); return true;`。删除孤儿 import(`java.util.Arrays`、`java.util.List`)。

- [ ] **Step 3: AuthController 挂到 /auth,写端点改名加锁**

类级 `@RequestMapping("/nullbot/auth")`。各方法:

```java
    @PostMapping("/regist")                     // 不变(放行)
    @PostMapping("/guest")                      // 不变(放行)
    @PostMapping("/login")                      // 不变(放行)

    @GetMapping("/me")                          // 原 GET /nullbot/info
    public WebResult<Map<String, Object>> info() { ... 原方法体不动 }

    @PutMapping("/me")                          // 原 POST /update
    public WebResult<Void> update(@RequestBody @Validated AdminDTO admin) {
        WebCtx.requireAdmin();
        ... 原 body 不变(id = WebCtx.getId())
    }

    @DeleteMapping("/me")                       // 原 DELETE /nullbot/delete
    public WebResult<Void> delete() { WebCtx.requireAdmin(); ... }

    @PutMapping("/me/password")                 // 原 POST /nullbot/password
    public WebResult<Void> changePassword(@RequestBody @Validated PasswordDTO password) {
        WebCtx.requireAdmin(); ... 
    }
```

注:除上列三个写方法首行加 `WebCtx.requireAdmin();` 外其余不变;`UnauthorizedException` 不再需要 handler,`./web/exception/handler/WebExceptionHandler` 中 `handleUnauthorized` 已删过,无操作。

- [ ] **Step 4: 编译**

Run: `mvn clean compile -q -o -DskipTests` → Expected: exit 0

- [ ] **Step 5: Commit** `refactor(web): RESTful auth 路由与 requireAdmin 鉴权`

