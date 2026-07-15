# ProjectForge Agent Notes

## 通用要求

- 整个分析过程以及结果均使用中文。
- 私有服务器凭据只能从 `C:\Users\19242\.codex\private.env` 临时读取，不得写入仓库、日志或回复。

## Repository

- GitHub: `https://github.com/calorsol/ProjectForge.git`
- Remote: `origin`
- Branch: `master`

## Production Topology

- App directory: `/home/web/projectforge`
- Static files: `/home/web/html/projectforge`
- Reverse proxy config: `/home/web/conf.d/projectforge.868601.xyz.conf`
- Public domain: `https://projectforge.868601.xyz`
- Backend host port: `127.0.0.1:8081`

## Deployment Shape

- 前端由 Nginx 从 `/home/web/html/projectforge` 提供静态文件。
- 后端 JAR 位于 `/home/web/projectforge/app/ppm-backend.jar`。
- MySQL 与后端由 `/home/web/projectforge/docker-compose.yml` 管理。
- 生产环境私有变量保存在 `/home/web/projectforge/.env`。
- Nginx 站点配置与仓库中的 `deploy/projectforge.868601.xyz.conf` 保持同步。

## Update Procedure

1. 本地验证：
   - 后端：在 `src/backend` 执行 `mvn test` 和 `mvn package -DskipTests`。
   - 前端：在 `src/frontend` 执行 `npm test` 和 `npm run build`。
2. 提交并推送 GitHub：
   - `git add ...`
   - `git commit -m "your message"`
   - `git push origin master`
3. 服务器更新：
   - 从 GitHub `master` 构建后端 JAR，并原子替换 `/home/web/projectforge/app/ppm-backend.jar`。
   - 同步 `src/frontend/dist` 到 `/home/web/html/projectforge`。
   - 执行 `docker compose up -d --force-recreate backend`。
4. 验证：
   - `docker compose ps`
   - 使用公网 HTTPS 地址验证登录和核心接口。

## Deployment Safety

- 禁止覆盖 `/home/web/projectforge/.env`。
- 禁止删除 MySQL 数据卷或 `/home/web/projectforge/uploads`。
- 不要影响 `/home/web` 下其他站点或容器。
- Nginx 配置变更后必须执行 `docker exec nginx nginx -t`，通过后才能 reload。
