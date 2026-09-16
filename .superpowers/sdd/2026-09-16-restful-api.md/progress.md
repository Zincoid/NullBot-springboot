# SDD ledger — plan: docs/superpowers/plans/2026-09-16-restful-api.md

BASE at start: 6f044aca (branch develop-260915)

## Pre-flight scan

| Pair/Task | Check | Finding | Ruling |
|---|---|---|---|
| T1↔T2..6 | T1 产出 requireAdmin/放行路径;T2-6 消费 | 无冲突(WebCtx 方法在 T1 落地,referenced only) |
| T2↔T7 | group.js 调 /groups、/settings/{gid} — 与 T2 路由一致 | clean |
| T5↔T7 | file api initApi 删除 ↔ 后端本无 /file/init | clean(两端都清) |
| T6 审计自查 | 🔒=38 与各任务加锁行数一致 | clean |
| T5 PUT body record 结构 | 计划给 record 写法,java record 在 controller 可行(Spring Web 支持紧凑 record body) | clean |
| AuthController PUT /auth/me body AdminDTO 含 id? | AdminDTO 可能无 id 字段,controller 用 WebCtx.getId() 补 — 与现 POST /update 一致 | clean |

Tasks ordered T1→T8, sequential dispatch.

## Progress

(pending)
Task 1: complete (commits 6f044aca..523c654f, review clean; minor deferred: commit message wording)
