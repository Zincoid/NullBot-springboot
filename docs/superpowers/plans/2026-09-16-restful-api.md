# RESTful API 重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** web API 从动词路由改为资源路由,访客鉴权改为方法级 `WebCtx.requireAdmin()`;前端 api 层同步。

**Architecture:** 后端只改 `web/controller` 注解路由与 `core/context/WebCtx`、`web/interceptor/WebInterceptor`;业务方法体不动(仅 inventory add 的路径参数与 DTO id 回写)。前端只改 `src/api/*.js` 与 4 个 view 的直拼 URL 字符串。

**Tech Stack:** Java 21 / Spring Boot 3 / MyBatis-Plus;Vue 3 / Vite / axios。

**Spec:** `docs/superpowers/specs/2026-09-16-restful-api-design.md`(本计划按 spec §2 的映射表逐控制器落任务;§2.2 的兜底条款落地为:保留 `GET /files/page` 端点不变,仅新增 `GET /files` 别名承载 search,不合并成功就在回退端点上保持两形状)。

## Global Constraints

- 根路径 `/nullbot` 不变;`WebResult{code,msg,data}` 与 HTTP 状态策略不变。
- OssController、websocket 层、服务层方法体一律不动。
- 🔒 端点(写操作与特权导出)方法体第一行:`WebCtx.requireAdmin();` + 导入 `com.zincoid.nullbot.core.context.WebCtx`。
- Maven: `& "C:\Users\Zincoid\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" clean compile -q -o -DskipTests`(必须 = 0);有界单测: `mvn.cmd test -q -o`(注意全量 test 在 PowerShell 下易卡住,不得后台撒手)。
- 前端 `request` baseURL `/api` 由 vite proxy 重写为 `/nullbot`,api 文件里的路径不含 `/nullbot`。
- 提交遵循 Conventional Commits;**每个任务编译通过后提交一次**(后端仓库 `NullBot-springboot`,前端任务提交 `NullBot-vue`)。
- DTO 已含 id 字段且带 @NotNull 校验;PUT 路径 id 与 body id 冲突时**以路径 id 为准**(覆盖 body)。

---

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

### Task 2: Group + Setting 控制器(耦合最高,settings 由 group 前端调用)

**Files:**
- Modify: `web/controller/GroupController.java`、`web/controller/SettingController.java`

GroupController:类级 `@RequestMapping("/nullbot/groups")`:

```java
    @GetMapping                         // 原 /list
    public WebResult<List<GroupPO>> getList() { 原体 }

    @GetMapping("/page")                // 不变
    @DeleteMapping("/{id}")             // 🔒 requireAdmin(); 原 /delete/{id}
    @PutMapping("/{id}")                // 🔒 requireAdmin(); 原 /update
    public WebResult<Void> delete(@PathVariable Long id) {
        WebCtx.requireAdmin();
        groupService.delete(id);
        ...
    }
    // update(@PathVariable Long id, @RequestBody @Valid GroupDTO group):
    //   WebCtx.requireAdmin(); group.setId(id); groupService.update(group); ...

    @GetMapping("/export")              // 🔒 requireAdmin(); 原 /exportCsv
    @PostMapping("/import")             // 🔒 requireAdmin(); 原 /importCsv
```

exportCsv/importCsv/export/import 均 void 返回,首行 `WebCtx.requireAdmin();`(import 再挂)。

SettingController:类级 `@RequestMapping("/nullbot/settings")`:

```java
    @GetMapping("/{id}")                                        // 不变
    @PutMapping("/{groupId}")                                  // 🔒 requireAdmin(); 原 PUT /set
    public WebResult<Void> set(@PathVariable Long groupId,
                               @RequestBody @Valid SettingDTO setting) {
        WebCtx.requireAdmin();
        setting.setGroupId(groupId);     // 路径 id 为准
        ... 原 body(使用 groupId 替代 setting.getGroupId())
    }
    @GetMapping("/export")  // 🔒 原 /exportCsv
    @PostMapping("/import") // 🔒 原 /importCsv (setAll)
```

- 编译 → Commit `refactor(web): group/setting 资源路由`

### Task 3: Item + User 控制器

同构,类级 `@RequestMapping("/nullbot/items")` / `/nullbot/users`:

```java
    @GetMapping                 // 原 /list
    @GetMapping("/page")        // 不变
    @GetMapping("/export")      // 🔒 原 /exportCsv
    @PostMapping("/import")     // 🔒 原 /importCsv
    @PostMapping                // 🔒 原 /add (仅 Item)
    @PutMapping("/{id}")        // 🔒 requireAdmin(); id 覆盖 DTO:
        // Item: item.setId(id);
        // User: user.setId(id);
    @DeleteMapping("/{id}")     // 🔒 原 /delete/{id}
```

注意 User 的 update/delete 方法签名是 `@PathVariable Long id`。Item 的 add 保持 `@RequestBody @Valid ItemDTO`;user 无 add 端点。

- 编译 → Commit `refactor(web): item/user 资源路由`

### Task 4: Inventory + Saying 控制器

InventoryController `@RequestMapping("/nullbot/inventories")`:

```java
    @GetMapping                         // 原 /list,带 @RequestParam Long userId
    @PostMapping                        // 🔒 @RequestParam Long userId, @RequestParam Integer itemId(保持 query 传参,管理员可为任意用户创建)
    @PutMapping("/{id}")                // 🔒 inventory.setId(id); 原 /update
    @DeleteMapping("/{id}")             // 🔒 原 /delete/{id}
    @GetMapping("/export")  // 🔒
    @PostMapping("/import") // 🔒
```

SayingController `@RequestMapping("/nullbot/sayings")`:

```java
    @GetMapping             // 原 /list
    @GetMapping("/page")    // 不变
    @DeleteMapping("/{id}") // 🔒 原 /delete/{id}
    @GetMapping("/export")  // 🔒
    @PostMapping("/import") // 🔒
```

- 编译 → Commit `refactor(web): inventory/saying 资源路由`

### Task 5: File + System + Stats 控制器

FileController `@RequestMapping("/nullbot/files")`(保持双端点兜底,返回形状不变):

```java
    @GetMapping                     // 原 /page (FileQuery query, 管理员全可见/访客 hidden)
    @GetMapping("/search")          // 不变保留(兜底条款)
    @PostMapping                    // 🔒 原 /upload (MultipartFile)
    @PostMapping("/dir")            // 🔒 原 /mkdir: requireAdmin(); fileService.mkdir(directory, name, WebCtx.getId())
    @GetMapping("/{id}/download")   // 原 /download/{id}
    @PUT /{id}/name:    🔒 rename(@PathVariable Integer id, @RequestBody Map<String,String> body) -> fileService.rename(id, body.get("filename"))
    @PUT /{id}/directory: 🔒 rename 类似, body key "directory"
    @PUT /{id}/visible:   🔒 body {"flag": true} 解析 Boolean
    @DELETE /{id}       🔒 原 /delete/{id}
    @PostMapping("/sync")           // 🔒 写操作改 POST, 原 GET /sync
```

PUT 子动作 body 用 `@RequestBody` + 小 record/静态内部类(如 `record NameBody(String filename) {}`,`record DirectoryBody(String directory) {}`,`record VisibleBody(Boolean flag) {}`)避免全局 map 弱类型。

SystemController `@RequestMapping("/nullbot/system")`:

```java
    @PostMapping("/invoke")     // 🔒 原 GET /invoke
    @PostMapping("/exception")  // 🔒 原 GET /exception
    @GetMapping("/func")        // 不变
    @PutMapping("/func")        // 🔒 原 PUT /func/set,参数保持 @RequestParam
    @GetMapping("/model")       // 不变
    @PutMapping("/model")       // 🔒 原 PUT /model/set
```

StatsController:`@RequestMapping("/nullbot/stats")` 保持,`@GetMapping` 不变 → **无改动**(仅核对)。

- 编译 → Commit `refactor(web): file/system 资源路由`

### Task 6: requireAdmin 对照表自查 + 全量测试

- [ ] **Step 1: 端点→🔐 对照自查**

用 `Select-String` 枚举 `web/controller/*.java` 里每个 `@(Get|Post|Put|Delete)Mapping` + 方法第一行,与 spec §2 表逐行核对。预期 🔒 总数 = **38**,构成:

| 组 | 🔒 端点 |
|---|---|
| group | delete / update(PUT)/ export / import = 4 |
| item | add / update / delete / export / import = 5 |
| user | update / delete / export / import = 4 |
| inventory | add / update / delete / export / import = 5 |
| saying | delete / export / import = 3 |
| setting | PUT(PUT /{groupId})/ export / import = 3 |
| files | upload / dir / name / directory / visible / delete / sync = 7 |
| auth | update / delete / password = 3 |
| system | invoke / exception / func / model = 4 |

漏加即补(一行 `WebCtx.requireAdmin();` + 视情况补 import),写法与 Task 2-5 相同。GET 端点中仅各 export 加锁;download/list/page/search/stats/me/settings/{id}/func/model 不加。

- [ ] **Step 2: 全仓搜残留旧路由**

`\b(delete|update|add|exportCsv|importCsv|list)/`、`/file/`、`/group/`、`/item/`、`/user/`、`/inventory/`、`/saying/`、`/setting/`、`/login`、`/regist`、`/info`、`/password` 等字面量在 src 下无残留(oss/auth 放行、`/auth/me` 除外)。

- [ ] **Step 3: 有界测试**

Run: `mvn.cmd test -q -o`(converter/序列化既有单测)→ Expected: 全 PASS(exit 0)。

- [ ] **Step 4: Commit** `chore(web): requireAdmin 审计通过,清理残留路由`

---

### Task 7: 前端 api 层(NullBot-vue)

**Files:**
- Modify: `src/api/{file,group,inventory,item,saying,stats,system,user}.js`(逐个同步旧→新路径与动词)
- Modify: `src/views/sub/{Group,Item,Saying,User}.vue`(4 处 `uploadAction('/xxx/importCsv')` 直拼 URL)

> 前端路径不含 `/api/`(baseURL 代理),逐文件替换(后端路由->前端调用):
> - group:  `/group/list`→`/groups`;`/group/page`→`/groups/page`;`/group/delete/{gid}`→`DELETE /groups/{gid}`;`/group/update`→`PUT /groups/{id}`(body 带 id 时会被服务端路径覆盖);`/group/exportCsv`→`/groups/export`;`/setting/{gid}`→`/settings/{gid}`;`/setting/set`→`PUT /settings/{body.groupId}`;`/setting/exportCsv`→`/settings/export`
> - item:   `/item/list`→`/items`;`/item/page`→`/items/page`;`/item/add`→`POST /items`;`/item/delete/{id}`→`DELETE /items/{id}`;`/item/update`→`PUT /items/{itemForm.id}`;`/item/exportCsv`→`/items/export`
> - user:   同 item 形状(无 add)
> - inventory: `/inventory/list`→`/inventories`;`/inventory/add (params userId,itemId)`→`POST /inventories` (params 同);`/inventory/delete/{id}`→`DELETE /inventories/{id}`;`/inventory/update`→`PUT /inventories/{id}`;`/inventory/exportCsv`→`/inventories/export`
> - saying: `/saying/list`→`/sayings`;`/saying/page`→`/sayings/page`;`/saying/delete/{id}`→`DELETE /sayings/{id}`;`/saying/exportCsv`→`/sayings/export`
> - stats/system(现有 `stats.js`/`system.js`):`/login`→`/auth/login`、`/guest`→`/auth/guest`、`/regist`→`/auth/regist`、`/info`→`/auth/me`、`/update`→`PUT /auth/me`、`/delete`→`DELETE /auth/me`、`/password`→`PUT /auth/me/password`、`/system/invoke`→`POST /system/invoke`(params 保持)、`/system/func/set`→`PUT /system/func`(params 保持)、`/system/model/set`→`PUT /system/model`(params 保持)
> - file:   `/file/page`→`/files`;`/file/search`→`/files/search`(保留);`/file/upload`→`POST /files`;`/file/download/{id}`→`/files/{id}/download`;`/file/mkdir`→`POST /files/dir` (params directory,name 保持);`/file/delete/{id}`→`DELETE /files/{id}`;`/file/rename/{id} (params fp)`→`PUT /files/{id}/name` + body `{filename}`;`/file/move/{id}`→`PUT /files/{id}/directory` + body `{directory}`;`/file/visualize/{id} (params flag)`→`PUT /files/{id}/visible` + body `{flag}`;`/file/sync`→`POST /files/sync`;**删除 `initApi`(后端无此端点)及其在 File.vue 的引用**
> - views:  Group.vue `uploadAction('/group/importCsv')`→`'/groups/import'`,`'/setting/importCsv'`→`'/settings/import'`;Item.vue `'/item/importCsv'`→`'/items/import'`;Saying.vue `'/saying/importCsv'`→`'/sayings/import'`;User.vue `'/user/importCsv'`→`'/users/import'`,`'/inventory/importCsv'`→`'/inventories/import'`

- [ ] **Step 1: 逐文件改 api 路径**(9 个文件全部按上表替换,注释里的中文描述不动)
- [ ] **Step 2: 4 个 view 的 uploadAction 字符串同步**
- [ ] **Step 3: 全仓 grep 残留**:旧路径字面量(`/list|/add|/delete/|/update|/exportCsv|/importCsv|/file/|/group/|/item/|/user/|/inventory/|/saying/|/setting/|/system/invoke$|/func/set|/model/set|/guest$|/info$`)在 `src/` 下无残留;router/index.js 的前端路由 `/login|/regist` 不算。
- [ ] **Step 4: 构建** `npm run build` → Expected: exit 0
- [ ] **Step 5: Commit**(NullBot-vue)`refactor(api): 前端接口同步 RESTful 路由`

### Task 8: 交叉审查

- [ ] 后端 `git diff` + 前端 `git diff` 对照 spec §2 表逐端点复查(重点 🔒 计数、PUT body 字段名与前端一致、`/file/init` 前后端都清干净)。
- [ ] `mvn clean compile` 后端、`npm run build` 前端(终验)通过。
