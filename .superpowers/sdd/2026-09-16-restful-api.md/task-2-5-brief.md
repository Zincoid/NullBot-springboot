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

