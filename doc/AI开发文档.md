# 个人项目平台 · AI 开发文档

> 本文档面向**执行开发的 AI**。请严格按照本文档实现，并**结合以下两个配套文件**理解需求与视觉：
> - 需求说明：[个人项目平台需求文档.md](./个人项目平台需求文档.md)
> - 交互原型（视觉与交互的唯一基准）：[../test/prototype.html](../test/prototype.html)
>
> 原则：**功能以需求文档为准，界面观感与交互以 prototype.html 为准。** 二者冲突时以本开发文档的显式约定优先，若仍不确定则在实现中保留 TODO 注释并选择最简合理方案。

- 文档版本：v1.0
- 编写日期：2026-07-14

---

## 0. 给 AI 开发者的执行须知

1. 先通读本文档 + 需求文档，再用浏览器打开 `test/prototype.html` 观察每个页面的布局、颜色、交互（登录、工作台 Tab、新建任务弹窗、创建项目弹窗、成员管理）。
2. 本项目是**前后端分离 + API 优先**。后端提供无状态 REST API，Web 前端与未来安卓端共用同一套接口。**所有接口必须与本文第 6 节契约完全一致（路径、方法、字段名）。**
3. 交付要求：代码可运行、有启动说明（README）、数据库有初始化脚本、内置一个超级管理员账号。
4. 代码风格：清晰、分层、命名语义化；关键业务逻辑（状态机、权限校验、文件存储）加注释。
5. 不要引入需求外的复杂功能（审批流、工时统计等）。保持轻量。

---

## 1. 技术栈（约定，除非有充分理由否则不要更换）

| 层 | 选型 | 版本 |
|----|------|------|
| 后端语言/框架 | Java + Spring Boot | Java 17, Spring Boot 3.2.x |
| 持久层 | MyBatis-Plus | 3.5.x |
| 数据库 | MySQL | 8.x |
| 鉴权 | JWT（jjwt 或 java-jwt） | - |
| 密码哈希 | BCrypt（Spring Security Crypto 的 BCryptPasswordEncoder，可单独引入，不强制引入完整 Spring Security） | - |
| 前端框架 | Vue 3 + Vite | Vue 3.4+, Vite 5+ |
| UI 组件库 | Element Plus | 2.x |
| 状态管理 | Pinia | 2.x |
| HTTP 客户端 | Axios | 1.x |
| 富文本编辑器 | wangEditor 5 或 TinyMCE（用于任务/项目描述，支持粘贴图片上传） | - |

> 备选：后端可用 Node.js + NestJS + TypeORM 实现同一套接口；如更换必须保证第 6 节接口契约不变。前端不要更换为 React。

---

## 2. 系统架构

```
┌─────────────┐        ┌─────────────┐         ┌──────────────┐
│  Web 前端   │        │  安卓端     │         │              │
│ Vue3+ElPlus │        │ (后续开发)  │         │   数据库      │
└──────┬──────┘        └──────┬──────┘         │  MySQL 8     │
       │  HTTP/JSON + JWT     │                │              │
       └──────────┬──────────┘                └──────▲───────┘
                  ▼                                   │
         ┌──────────────────┐    文件读写     ┌───────┴────────┐
         │  后端 REST API   │───────────────▶│ 服务器文件目录  │
         │  Spring Boot     │                │ /uploads/yyyy/MM│
         └──────────────────┘                └────────────────┘
```

- 前端 `dev` 通过 Vite 代理把 `/api` 转发到后端；生产由 Nginx 或后端静态托管。
- 所有受保护接口需在 Header 携带 `Authorization: Bearer <token>`。

---

## 3. 目录结构（建议）

### 后端 `backend/`
```
backend/
├── pom.xml
├── src/main/java/com/example/ppm/
│   ├── PpmApplication.java
│   ├── config/          # 跨域、JWT拦截器、MyBatis-Plus配置、文件存储配置
│   ├── controller/      # AuthController, UserController, ProjectController, TaskController, AttachmentController
│   ├── service/         # 业务逻辑 + impl
│   ├── mapper/          # MyBatis-Plus Mapper
│   ├── entity/          # User, Project, Task, Attachment
│   ├── dto/             # 请求/响应体
│   ├── common/          # 统一返回R<T>、异常处理、枚举、常量
│   └── security/        # JWT工具、当前用户上下文、权限注解
└── src/main/resources/
    ├── application.yml
    └── db/schema.sql     # 建表 + 初始化超级管理员
```

### 前端 `frontend/`
```
frontend/
├── package.json
├── vite.config.js       # 配置 /api 代理
├── index.html
└── src/
    ├── main.js
    ├── App.vue
    ├── router/index.js          # 路由 + 登录守卫 + 管理员守卫
    ├── store/user.js            # Pinia: token, 用户信息, 角色
    ├── api/                     # 封装 axios: auth.js, project.js, task.js, user.js, attachment.js
    ├── layout/AppLayout.vue     # 顶部导航栏（工作台/项目/成员管理）
    └── views/
        ├── Login.vue            # 登录/注册
        ├── Workbench.vue        # 工作台（首页）
        ├── Projects.vue         # 项目列表 + 创建项目弹窗
        ├── ProjectDetail.vue    # 项目下的任务（可选）
        ├── Members.vue          # 成员管理（仅管理员）
        └── components/
            ├── TaskModal.vue    # 新建/编辑任务弹窗
            └── ProjectModal.vue # 创建/编辑项目弹窗
```

---

## 4. 数据库设计（DDL，直接可用）

```sql
CREATE DATABASE IF NOT EXISTS ppm DEFAULT CHARACTER SET utf8mb4;
USE ppm;

-- 用户
CREATE TABLE `user` (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'USER',      -- ADMIN / USER
  status        VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',   -- ENABLED / DISABLED
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login_at DATETIME     NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0
);

-- 项目
CREATE TABLE `project` (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  code        VARCHAR(50)  NOT NULL,
  owner_id    BIGINT       NOT NULL,                       -- 归属用户
  sort_no     INT          NOT NULL DEFAULT 0,
  description LONGTEXT      NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',      -- ACTIVE / DONE / ARCHIVED
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  INDEX idx_owner (owner_id)
);

-- 任务
CREATE TABLE `task` (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  title         VARCHAR(200) NOT NULL,
  project_id    BIGINT       NULL,                         -- 所属项目，可空=默认项目
  owner_id      BIGINT       NOT NULL,                     -- 归属/创建用户（本人）
  type          VARCHAR(20)  NULL,                         -- 开发/修复/测试/调研/杂务...
  priority      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',    -- HIGH/MEDIUM/LOW
  status        VARCHAR(10)  NOT NULL DEFAULT 'TODO',      -- TODO/DOING/PAUSED/DONE/CANCELED
  plan_start_at DATETIME     NULL,
  due_at        DATETIME     NULL,
  description   LONGTEXT     NULL,                          -- 富文本HTML，可含<img>
  finished_at   DATETIME     NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  INDEX idx_owner (owner_id),
  INDEX idx_project (project_id),
  INDEX idx_status (status)
);

-- 图片/附件
CREATE TABLE `attachment` (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id     BIGINT       NULL,                            -- 关联任务（描述内嵌图片可先无task_id，保存任务时回填）
  kind        VARCHAR(10)  NOT NULL DEFAULT 'FILE',         -- IMAGE / FILE
  file_name   VARCHAR(255) NOT NULL,
  file_path   VARCHAR(500) NOT NULL,                        -- 服务器相对路径
  url         VARCHAR(500) NOT NULL,                        -- 访问URL
  size        BIGINT       NOT NULL DEFAULT 0,
  mime_type   VARCHAR(100) NULL,
  uploaded_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_task (task_id)
);

-- 初始化超级管理员：用户名 admin / 密码 admin123 （password_hash 为该密码的 BCrypt 值，务必在启动时用代码校验或替换）
-- 建议由后端启动时检测：若无 admin 用户则用 BCryptPasswordEncoder 生成哈希并插入，避免硬编码错误哈希。
INSERT INTO `user` (username, password_hash, role, status)
SELECT 'admin', '$2a$10$PLACEHOLDER_REPLACE_AT_STARTUP', 'ADMIN', 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'admin');
```

> **超级管理员初始化**：不要依赖上面占位哈希。请在后端启动时（`ApplicationRunner`）检查是否存在 `admin`，不存在则用 `BCryptPasswordEncoder` 对默认密码 `admin123` 加密后插入。首次登录后提示修改密码。

---

## 5. 通用约定

### 5.1 统一响应结构
所有接口返回：
```json
{ "code": 0, "message": "ok", "data": {} }
```
- `code`：0 成功；非 0 失败（如 401 未登录、403 无权限、400 参数错误、500 服务器错误）。
- 前端 axios 拦截器统一处理：`code!=0` 弹出 `message`；`code==401` 跳转登录。

### 5.2 鉴权
- 登录成功返回 JWT，前端存 `localStorage` 并放入 Pinia。
- 后端用拦截器/过滤器校验除 `/api/auth/login`、`/api/auth/register` 外的接口。
- JWT 载荷含 `userId`、`username`、`role`。
- 管理员接口（`/api/admin/**`）额外校验 `role==ADMIN`，否则返回 403。

### 5.3 当前用户数据隔离
- 项目、任务、附件的读写都限定在 `owner_id == 当前用户`（管理员的成员管理接口除外）。
- 任何跨用户访问返回 403 或空。

---

## 6. 接口契约（必须严格实现）

> 前缀统一 `/api`。除登录/注册外均需 `Authorization: Bearer <token>`。

### 6.1 认证 Auth

**POST /api/auth/register** — 注册
```json
// 请求
{ "username": "zhangsan", "password": "123456", "confirmPassword": "123456" }
// 响应 data
{ "id": 2, "username": "zhangsan" }
```
校验：username 唯一、2–20 字符；password ≥6；两次密码一致。默认 role=USER, status=ENABLED。

**POST /api/auth/login** — 登录
```json
// 请求
{ "username": "admin", "password": "admin123" }
// 响应 data
{ "token": "xxx.yyy.zzz", "user": { "id":1, "username":"admin", "role":"ADMIN" } }
```
失败："用户名或密码错误"；禁用："账号已被禁用，请联系管理员"。成功更新 `last_login_at`。

**POST /api/auth/logout** — 退出（前端清 token 即可，后端可空实现返回成功）

**GET /api/auth/me** — 当前用户信息 → `{ id, username, role }`

### 6.2 成员管理 Admin（仅 ADMIN）

**GET /api/admin/users?keyword=** — 用户列表
```json
// 响应 data: 数组
[ { "id":2, "username":"zhangsan", "role":"USER", "status":"ENABLED",
    "createdAt":"2026-06-15 10:00:00", "lastLoginAt":"2026-07-13 21:03:00" } ]
```
**PUT /api/admin/users/{id}/status** — `{ "status": "DISABLED" }` 启用/禁用
**PUT /api/admin/users/{id}/role** — `{ "role": "ADMIN" }` 设置角色
**PUT /api/admin/users/{id}/password** — `{ "newPassword": "xxxxxx" }` 重置密码
**DELETE /api/admin/users/{id}** — 软删除（保留其任务数据）
> 约束：不允许禁用/删除/降级最后一个 ADMIN；不允许 admin 删除自己。

### 6.3 项目 Project

**GET /api/projects?keyword=&status=** — 项目列表（含统计）
```json
// 响应 data: 数组
[ { "id":1, "name":"网站重构", "code":"web_refactor", "sortNo":30089,
    "status":"ACTIVE", "createdAt":"2026-06-20 09:00:00",
    "taskTotal":3, "taskOpen":2 } ]
```
`taskTotal`=项目下未删除任务数，`taskOpen`=状态非 DONE/CANCELED 的任务数。按 `sortNo` 升序。

**POST /api/projects** — 创建项目
```json
{ "name":"网站重构", "code":"web_refactor", "sortNo":30089, "description":"<p>...</p>" }
```
校验：name、code 必填。owner_id 取当前用户。

**PUT /api/projects/{id}** — 编辑项目（同上字段）
**GET /api/projects/{id}** — 项目详情

### 6.4 任务 Task

**GET /api/tasks** — 任务列表，支持 query 参数：
`keyword`(标题模糊), `projectId`, `type`, `status`, `startFrom`, `startTo`
```json
// 响应 data: 数组
[ { "id":10, "title":"重构登录页样式", "projectId":1, "projectName":"网站重构",
    "type":"开发", "priority":"HIGH", "status":"DOING",
    "planStartAt":"2026-07-13 09:00:00", "dueAt":"2026-07-16 18:00:00",
    "overdue":false, "createdAt":"...", "finishedAt":null } ]
```
`overdue` 由后端计算：`dueAt < now() 且 status ∈ {TODO,DOING,PAUSED}`。

**GET /api/workbench/tasks?tab=** — 工作台任务，`tab` ∈ `todo|paused|done|all`
- `todo`：status ∈ {TODO, DOING}
- `paused`：status = PAUSED
- `done`：status = DONE
- `all`：全部（未删除）
均限定当前用户，返回结构同上。

**POST /api/tasks** — 创建任务
```json
{ "title":"...", "projectId":1, "type":"开发", "priority":"MEDIUM",
  "planStartAt":"2026-07-13 09:00:00", "dueAt":"2026-07-16 18:00:00",
  "description":"<p>富文本HTML</p>", "attachmentIds":[5,6] }
```
- `title` 必填；`projectId` 可空（空=默认项目，见 7.4）；status 初始 TODO。
- `attachmentIds`：先上传得到的附件 id 列表，创建时回填这些附件的 `task_id`。

**PUT /api/tasks/{id}** — 编辑任务（同上字段）
**DELETE /api/tasks/{id}** — 软删除
**GET /api/tasks/{id}** — 任务详情（含 attachments 数组）

**PATCH /api/tasks/{id}/status** — 状态变更（核心）
```json
{ "action": "start" }   // start|pause|resume|finish|cancel|reopen
```
状态机见第 7.1 节。`finish` 时写入 `finished_at=now()`；`reopen` 清空 `finished_at` 并置 DOING。非法流转返回 400。

### 6.5 图片 / 附件 Attachment

**POST /api/attachments** — 上传（multipart/form-data）
- 表单字段：`file`（文件）、`kind`（IMAGE/FILE，可选，默认按 mime 判断）
```json
// 响应 data
{ "id":5, "kind":"IMAGE", "fileName":"screenshot.png",
  "url":"/uploads/2026/07/uuid.png", "size":20480, "mimeType":"image/png" }
```
- 富文本编辑器粘贴图片时调用此接口，用返回 `url` 插入 `<img>`。
- 校验：IMAGE ≤10MB 且 mime 为 image/*；FILE ≤50MB。超限返回 400。

**GET /api/tasks/{id}/attachments** — 某任务的附件列表
**DELETE /api/attachments/{id}** — 删除记录并删除服务器文件

---

## 7. 关键业务规则

### 7.1 任务状态机（务必按此校验 action 合法性）
```
状态: TODO(未开始) DOING(进行中) PAUSED(已暂停) DONE(已完成) CANCELED(已取消)

TODO   --start-->  DOING
DOING  --pause-->  PAUSED
PAUSED --resume--> DOING
DOING  --finish--> DONE
PAUSED --finish--> DONE
TODO   --cancel--> CANCELED
DOING  --cancel--> CANCELED
PAUSED --cancel--> CANCELED
DONE   --reopen--> DOING
```
其他任意 (状态, action) 组合非法 → 返回 400「非法的状态操作」。

### 7.2 权限矩阵
| 功能 | USER | ADMIN |
|------|:----:|:-----:|
| 注册/登录/改自己密码 | ✅ | ✅ |
| 自己的项目/任务 CRUD、状态操作、附件 | ✅ | ✅ |
| 成员管理（/api/admin/**） | ❌(403) | ✅ |
- 前端：普通用户导航栏隐藏「成员管理」；后端必须独立做服务端校验，不能只靠前端隐藏。

### 7.3 逾期判断
- 列表与工作台返回 `overdue` 布尔；前端逾期任务的截止时间标红（见 prototype 中 `.overdue` 样式）。

### 7.4 默认项目
- 每个用户首次创建任务且未选项目时，后端自动为其创建/复用一个 `code=default`、`name=默认项目` 的项目并归入。或允许 task.project_id 为 NULL 并在展示时显示「默认项目」。二选一，推荐后者（NULL）以简化。

### 7.5 文件存储
- 存储根目录可配置（application.yml: `app.upload-dir=./uploads`）。
- 子目录按 `yyyy/MM` 分。文件名用 UUID + 原扩展名，避免冲突与中文问题。
- 通过静态资源映射把 `app.upload-dir` 暴露为 `/uploads/**` 供前端访问。

---

## 8. 前端页面实现映射（对照 prototype.html）

| 页面/组件 | 对应 prototype 部分 | 关键点 |
|-----------|--------------------|--------|
| `Login.vue` | `#login` | 登录/注册切换；仅用户名+密码（+确认密码）；成功后存 token 并跳工作台 |
| `AppLayout.vue` | `.topbar` | 蓝色渐变导航栏；右上角显示用户名+角色+退出；「成员管理」仅 ADMIN 显示 |
| `Workbench.vue` | `#view-workbench` | Tab: 待办/已暂停/已完成/全部；表格列：任务名称/所属项目/类型/优先级/计划开始/截止时间/状态/操作；行内状态按钮按状态机显示（开始▶/暂停⏸/继续▶/完成✓/编辑/删除/重开↻） |
| `TaskModal.vue` | `#taskModal` | 弹窗宽≈860px；字段：任务名称*/所属项目/类型/优先级/时间计划(开始~截止)/描述(富文本,可粘贴图片)/图片/附件；描述用富文本编辑器；图片与附件调上传接口 |
| `Projects.vue` | `#view-projects` | 表格列：项目名称/编号/状态/任务总数/未完成/创建时间/操作；「创建项目」按钮开 ProjectModal |
| `ProjectModal.vue` | `#projectModal` | 弹窗宽≈720px；字段：项目名称*/项目编号*/排序号/项目描述(富文本)。**无「负责人」字段**（个人平台） |
| `Members.vue` | `#view-members` | 仅 ADMIN；表格列：用户名/角色/状态/注册时间/最近登录/操作(重置密码/启用禁用/设角色/删除) |

**视觉基准**（取自 prototype 的 CSS 变量，前端主题尽量对齐）：
- 主色蓝 `#2c7be5`，导航渐变 `#2c7be5→#1b64c4`
- 状态标签：进行中橙 `#ff9a2e`、已完成绿 `#00b42a`、未开始/已暂停灰、逾期红 `#f53f3f`
- 卡片圆角 8px、表头浅灰 `#fafbfc`

---

## 9. 开发里程碑（建议顺序）

1. **M1 后端地基**：项目搭建、MySQL 连接、建表脚本、统一响应 R<T>、全局异常处理、JWT 工具 + 拦截器、启动初始化 admin。
2. **M2 认证**：注册/登录/me/logout，前端登录页 + 路由守卫 + Pinia。
3. **M3 项目**：项目 CRUD + 统计；前端项目列表 + 创建项目弹窗。
4. **M4 任务**：任务 CRUD + 列表筛选 + 状态机 PATCH；前端工作台 + 任务弹窗。
5. **M5 附件**：文件上传/删除 + 静态资源映射 + 富文本粘贴图片；前端图片/附件区。
6. **M6 成员管理**：admin 接口 + 前端成员管理页 + 管理员守卫。
7. **M7 收尾**：README、跨域配置、逾期标红、默认项目处理、联调与自测。

---

## 10. 验收标准（Definition of Done）

- [ ] 后端可独立启动，README 说明数据库配置与启动命令。
- [ ] 首次启动自动创建 `admin/admin123` 超级管理员。
- [ ] 注册新用户 → 用新用户登录 → 看不到「成员管理」；直接调 `/api/admin/users` 返回 403。
- [ ] 创建项目 → 新建任务并选该项目 → 工作台各 Tab 数据正确。
- [ ] 任务状态按状态机流转，非法流转被拒；完成时间正确记录。
- [ ] 任务描述可粘贴截图并保存，图片持久化到服务器且刷新后仍可显示。
- [ ] 附件可上传任意文件、下载、删除，文件真实存在于服务器目录。
- [ ] 逾期任务在列表/工作台截止时间标红。
- [ ] 管理员可重置密码、启用/禁用、设角色、删除用户；不能操作最后一个管理员或删除自己。
- [ ] 所有接口路径/字段与第 6 节契约一致（为安卓端复用打基础）。

---

## 11. 后续扩展（本期不做，接口预留即可）
- 安卓客户端（复用同一 REST API）。
- 到期提醒 / 推送通知。
- 今日/本周待办统计卡片、完成率图表。
