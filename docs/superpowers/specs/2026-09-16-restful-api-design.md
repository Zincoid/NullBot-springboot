# NullBot Web API RESTful 化设计

日期: 2026-09-16
范围: NullBot-springboot(web 层)+ NullBot-vue(api 层)
基线: 后端 HEAD `fa3051d7`,分支 `develop-260915`(22+ 未推送提交之后)

## 1. 目标与非目标

**目标**:web REST 接口从动词路由改为资源路由;访客鉴权从"URL 黑名单子串匹配"改为"控制器方法显式 `WebCtx.requireAdmin()`"。

**不做**:
- OssController(`/nullbot/oss`,ResponseEntity 特例)不动
- WebSocket/STOMP 不动
- 返回结构 `WebResult{code,msg,data}` 不变,HTTP 状态码策略不变(仍 200+fail body)
- 业务逻辑、服务层不动

## 2. URL 映射

根路径保留 `/nullbot`。资源名词:`groups/items/users/inventories/sayings/settings/files`。

### 2.1 CRUD 资源五个(group/item/user/inventory/saying)

| 旧 | 新 | 备注 |
|---|---|---|
| GET /{rs}/list | GET /{rs} | 全量列表 |
| GET /{rs}/page | GET /{rs}/page | 保留分页端点(拒绝合并——返回形状不同,前端分页组件依赖 PageResult) |
| POST /{rs}/add | POST /{rs} | 🔒 |
| PUT /{rs}/update | PUT /{rs}/{id} | 🔒 |
| DELETE /{rs}/delete/{id} | DELETE /{rs}/{id} | 🔒 |
| GET /{rs}/exportCsv | GET /{rs}/export | 🔒 |
| POST /{rs}/importCsv | POST /{rs}/import | 🔒 |

🔒 = 方法开头加 `WebCtx.requireAdmin()`。

- settings 组特殊:原 `POST /nullbot/setting/set` → `PUT /settings/{groupId}`(groupId 取自路径,与 GET 同形);`GET /settings/{groupId}` 不加锁。
- inventory 的新增:body 只送 `itemId/amount`(InventoryDTO 语义不变),userId 一律由服务端取 `WebCtx.getId()`,不信前端;若 DTO 存在 userId 字段则实现时显式覆盖。

### 2.2 files

| 旧 | 新 | 备注 |
|---|---|---|
| GET /file/page | GET /files | 查询(带 query 参数);对管理员可见性过滤逻辑不变 |
| GET /file/search | GET /files | 用同名 `?keyword=&directory=` 参数与 page 合并为一个查询端点(page 用 query 传分页参) |
| POST /file/upload | POST /files | multipart 🔒 |
| GET /file/download/{id} | GET /files/{id}/download | 不加锁(访客可下载可见文件,同现状) |
| GET /file/mkdir | POST /files/dir | 🔒(目录创建是写;落在 /files/dir 子动作,与 upload 区分) |
| DELETE /file/delete/{id} | DELETE /files/{id} | 🔒 |
| GET /file/rename/{id} | PUT /files/{id}/name | 🔒 filename 走 body |
| GET /file/move/{id} | PUT /files/{id}/directory | 🔒 directory 走 body |
| GET /file/visualize/{id} | PUT /files/{id}/visible | 🔒 flag 走 body |
| GET /file/sync | POST /files/sync | 🔒(写操作,改 POST) |

> page+search 合并:两者均为 GET /files,`page(FileQuery)` 与 `search(keyword, directory)` 后端方法保留两个路由可选——实现简化:一个 `GET /files` 同时接受 query 与 keyword(`keyword` 存在走 search 语义)。若实现中出现行为纠结,退化为保留 `GET /files`(搜索)与 `GET /files/page` 两个端点,**保持返回形状不变**优先。

### 2.3 auth(自管理)

| 旧 | 新 | 备注 |
|---|---|---|
| POST /nullbot/login | POST /auth/login | 拦截器放行路径同步 |
| POST /nullbot/regist | POST /auth/regist | 放行 |
| POST /nullbot/guest | POST /auth/guest | 放行 |
| POST /nullbot/update | PUT /auth/me | 🔒 身份取自 token |
| DELETE /nullbot/delete | DELETE /auth/me | 🔒 |
| POST /nullbot/password | PUT /auth/me/password | 🔒 |
| GET /nullbot/info | GET /auth/me | 放行(访客/管理员均可读自身信息) |

### 2.4 stats / system / oss

| 旧 | 新 | 备注 |
|---|---|---|
| GET /nullbot/stats | GET /stats | |
| GET /system/invoke | POST /system/invoke | 🔒 执行代码类,写副作用 |
| GET /system/exception | POST /system/exception | 🔒 |
| GET /system/func | GET /system/func | |
| PUT /system/func/set | PUT /system/func | 🔒 |
| GET /system/model | GET /system/model | |
| PUT /system/model/set | PUT /system/model | 🔒 |
| oss | 不动 | 拦截器 `/nullbot/oss` 放行不变 |

## 3. 鉴权(单层 requireAdmin)

### 3.1 WebCtx 新增

```java
public static void requireAdmin() {
    if (getType() == null)
        throw new CoreException("未登录");
    if (getType() != 1)
        throw new CoreException("访客受限");
}
```

- 抛 `CoreException` → `WebExceptionHandler.handleCoreException` → `WebResult.fail(200)`,前端无感知,同现"访客受限"体验。
- `getType()==null` 防御:直连(不经拦截器的线程,如测试直调)不再 NPE。

### 3.2 拦截器同步(`WebInterceptor`)

- 放行路径字面量改为 `/nullbot/auth/login|regist|guest`;`/nullbot/oss` 保留。
- **删除 GUEST_FORBIDDEN_URLS 静态黑名单与访客循环**——访客与管理员在拦截器中一律只做 token 校验后放行,权限差异全部由控制器方法内 requireAdmin 承担。
- 已知取舍(与用户确认):写端点漏加 requireAdmin = 访客可写,无兜底。实现完成后用 §5 对照表自查。

## 4. 前端(NullBot-vue)

- `src/api/{file,group,inventory,item,saying,stats,system,user}.js` 全部 URL/方法名同步(settings 相关调用在 Center.vue/相应 api 文件中一并改)。
- `src/utils/request.js` 不动(baseURL/拦截器无关)。
- 检查 views(`Login/Regist/Center/File/...`)与 `utils/save.js`、`rules/*` 是否有直拼 URL 字符串直调后端,发现即同步。
- `npm run build` 通过为验证门槛。

## 5. 验证

1. 后端 `mvn clean compile -q -o -DskipTests` = 0
2. `mvn test -q -o`(converter/序列化既有单测全绿)
3. **requireAdmin 对照自查**:逐个端点枚举"新 URL → 是否加锁",与 §2 表格逐行核对(重点:5×export、system 4 个写端点、auth 3 个写端点、files 6 个写端点、5×4=20 CRUD 写端点)
4. 前端 build 通过
5. 两仓库同批提交,避免中间态部署

## 6. 风险与缓解

- **requireAdmin 漏加** → 写漏洞。缓解:§5.3 对照清单逐端点核对 + 交叉审查。
- **list/page 合并若破坏返回形状** → 实现 §2.2 兜底条款:保持/shapes 不变优先于端点数最小化。
- **前端遗漏直调 URL** → §4 的全仓搜索(grep `nullbot` 字面量)兜底。
- **访客体验变化** → 失败信息从"访客受限"拦截响应变为 CoreException 失败体,code/msg 结构相同。
