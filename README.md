# 个人项目平台

Vue 3 + Spring Boot 的个人任务与项目管理平台。接口前缀、请求字段和响应格式遵循 `doc/AI开发文档.md` 第 6 节。

## 环境

- JDK 17、Maven 3.9+、MySQL 8
- Node.js 20+

## 数据库与后端

1. 在 MySQL 创建可连接的用户，并在启动前设置本地环境变量（不要把密码写入仓库）：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的MySQL密码"
```

2. `src/backend/src/main/resources/db/schema.sql` 会在启动时创建数据库和表。
3. 运行：

```powershell
cd src/backend
mvn spring-boot:run
```

首次启动会创建超级管理员：`admin` / `admin123`。

上传文件存放在后端工作目录的 `uploads/yyyy/MM`，通过 `/uploads/**` 提供访问。

## 前端

```powershell
cd src/frontend
npm install
npm run dev
```

浏览器访问 Vite 输出的地址（默认 `http://localhost:5173`）。开发服务器自动把 `/api` 转发到 `http://localhost:8080`。

## 验证

```powershell
cd src/backend; mvn test
cd ../frontend; npm test; npm run build
```
