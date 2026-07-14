# Personal Project Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a Vue 3 and Spring Boot personal project platform with the API contract defined in `doc/AI开发文档.md`.

**Architecture:** `src/backend` exposes stateless JWT-protected REST endpoints and owns MySQL persistence plus local upload storage. `src/frontend` calls only the documented `/api` contract, stores the token in Pinia/localStorage, and mirrors `test/prototype.html` using Element Plus pages and dialogs.

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus, MySQL 8, JJWT, BCrypt, Vue 3, Vite, Element Plus, Pinia, Axios, wangEditor 5, JUnit 5, Vitest.

---

### Task 1: M1 Backend foundation

**Files:** Create `src/backend/pom.xml`, `src/backend/src/main/java/com/example/ppm/**`, `src/backend/src/main/resources/application.yml`, `src/backend/src/main/resources/db/schema.sql`, and focused JUnit tests in `src/backend/src/test/java/com/example/ppm/**`.

- [ ] Write failing tests for `R.ok`, `R.fail`, JWT parsing/rejection, and the admin bootstrap behavior.
- [ ] Run `mvn test` and confirm each test fails because its production class is absent.
- [ ] Add the minimal Spring Boot application, common response/error types, configuration, JWT filter/context, BCrypt bean, DDL, and `ApplicationRunner` that creates `admin/admin123` only when absent.
- [ ] Run `mvn test` and `mvn package -DskipTests`; confirm both exit 0.

### Task 2: M2 Authentication

**Files:** Create backend auth entity/mapper/service/controller/DTO/tests; create frontend Vite files, API client, Pinia user store, router, `App.vue`, and `views/Login.vue`.

- [ ] Write failing endpoint/service tests for registration validation, login token output, disabled account rejection, and `/api/auth/me`.
- [ ] Implement precisely `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`, and `GET /api/auth/me`.
- [ ] Write failing Vitest cases for token persistence and redirect guard; implement the minimal Axios interceptors, store, router, and login/register toggle.
- [ ] Run backend tests, `npm run build`, and frontend tests.

### Task 3: M3 Projects

**Files:** Create backend project mapper/service/controller/DTO/tests and frontend project API, `Projects.vue`, `ProjectModal.vue`.

- [ ] Write failing tests for owner isolation, required name/code, sort ordering, and `taskTotal`/`taskOpen` statistics.
- [ ] Implement exact project endpoints `GET/POST /api/projects`, `GET/PUT /api/projects/{id}`.
- [ ] Implement prototype-aligned project table, filters, and create/edit dialog.
- [ ] Run backend tests and frontend build.

### Task 4: M4 Tasks and workbench

**Files:** Create task mapper/service/controller/DTO/tests and frontend task API, `Workbench.vue`, `TaskModal.vue`, `ProjectDetail.vue`.

- [ ] Write failing tests for task owner isolation, documented filters, all valid state transitions, invalid transition rejection, `finishedAt`, and overdue calculation.
- [ ] Implement documented task CRUD, `PATCH /api/tasks/{id}/status`, and `GET /api/workbench/tasks?tab=`.
- [ ] Implement workbench tabs/filter toolbar, status action icons, overdue styling, and task dialog fields.
- [ ] Run backend tests and frontend build.

### Task 5: M5 Attachments and rich text

**Files:** Create attachment mapper/service/controller/DTO/tests and update backend storage config; add frontend attachment API and enhance `TaskModal.vue`.

- [ ] Write failing tests for MIME/size limits, owner checks, date-partitioned UUID storage metadata, and attachment assignment to a task.
- [ ] Implement exact upload/list/delete endpoints, `/uploads/**` resource mapping, and task attachment ownership/backfill.
- [ ] Integrate wangEditor image upload with `/api/attachments`, thumbnail preview, file list/download/delete, and pasted screenshot upload.
- [ ] Run backend tests and frontend build.

### Task 6: M6 Member administration

**Files:** Create admin DTO/service/controller/tests and frontend user API plus `Members.vue`; update layout and route guard.

- [ ] Write failing tests for admin-only access, keyword filtering, self-delete prevention, and protection of the final administrator.
- [ ] Implement every documented `/api/admin/users` endpoint including soft delete.
- [ ] Implement the admin-only navigation/page, table, password reset prompt, status and role actions.
- [ ] Run backend tests and frontend build.

### Task 7: M7 integration and delivery

**Files:** Create `README.md`; update application/Vite config and all relevant UI styles.

- [ ] Execute an API smoke-test sequence: bootstrap admin; register/login user; verify 403 on admin endpoint; create project/task; exercise status actions; upload/delete attachment.
- [ ] Build both applications from a clean dependency state and validate all tests.
- [ ] Document Java, Maven, Node, MySQL requirements, database configuration, backend/frontend start commands, upload location, and `admin/admin123` credentials.
