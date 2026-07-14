# projectforge Agent Notes

额外：

1、整个分析过程以及结果均用中文。

## Repository

- GitHub: `https://github.com/calorsol/ProjectForge.git`
- Remote: `origin`

## Production Topology

- Server: `root@64.83.35.146`
- Username: `root`
- Password: `aA19971108`
- Base directory: `/home/web`
- App directory: `/home/web/projectforge`
- Reverse proxy config: `/home/web/conf.d/projectforge.868601.xyz.conf`
- Public domain: `https://projectforge.868601.xyz`
- App container port: `4000`

## Current Deployment Shape

- `/home/web` is the shared web root on the server.
- Other sites on this machine also follow the `/home/web/conf.d/*.conf` nginx layout.
- projectforge itself is updated inside `/home/web/projectforge`.
- The running app is managed with `docker compose`.
- `deploy/projectforge.868601.xyz.conf` in this repo mirrors the nginx site config used on the server.

## Update Deployment Procedure

1. Verify locally before shipping:
   - `npm test`
   - `npm run build:client`
2. Push the code to GitHub:
   - `git add ...`
   - `git commit -m "your message"`
   - `git push origin main`
3. Package the project for the server from the repo root.
   - Do not include `.git`, `node_modules`, local logs, `.env`, or runtime data.
   - Suggested command:

```bash
tar --exclude='./.git' \
    --exclude='./node_modules' \
    --exclude='./dist' \
    --exclude='./src/client/dist' \
    --exclude='./.env' \
    --exclude='./data' \
    --exclude='./src/server/data' \
    --exclude='*.log' \
    -czf /tmp/projectforge.tar.gz .
```

4. Upload the archive to the existing deployment directory:

```bash
scp /tmp/projectforge.tar.gz root@64.83.35.146:/home/web/projectforge/
```

5. Extract and refresh the app on the server:

```bash
ssh root@64.83.35.146 "cd /home/web/projectforge && tar -xzf projectforge.tar.gz && rm -f projectforge.tar.gz && docker compose up -d --build"
```

6. Verify the update:

```bash
ssh root@64.83.35.146 "docker ps --filter name=projectforge"
curl -s https://projectforge.868601.xyz/api/health
curl -I https://projectforge.868601.xyz/
```

## Deployment Safety Notes

- Do not overwrite `/home/web/projectforge/.env`.
- Do not delete or replace `/home/web/projectforge/data`; it contains the persisted SQLite database.
- If nginx config changes are required, update both:
  - `/home/web/conf.d/projectforge.868601.xyz.conf`
  - `deploy/projectforge.868601.xyz.conf`
- After nginx config changes, reload nginx on the server:

```bash
ssh root@64.83.35.146 "docker exec nginx nginx -t && docker exec nginx nginx -s reload"
```
