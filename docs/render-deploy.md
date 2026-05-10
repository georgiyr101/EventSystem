# Deploy on Render (app) + Neon (PostgreSQL)

## 1. Database (Neon)

1. Create a project at [https://neon.tech](https://neon.tech).
2. Create a database and copy the **connection string** (PostgreSQL).
3. Build the JDBC URL Spring expects, for example:
   - `jdbc:postgresql://<host>/<database>?sslmode=require`
4. Note **user** and **password** from the Neon console if they are not embedded in the URL you prefer to use.

## 2. Render Web Service

1. In the [Render dashboard](https://dashboard.render.com), create a **Web Service**.
2. Connect your Git repository (or use **Docker** with this repo’s `Dockerfile`).
3. Runtime: **Docker** (root directory `.`, Dockerfile path `Dockerfile`).
4. Instance type: **Free** (cold starts apply).

## 3. Environment variables on Render

Set at least:

| Variable | Example / notes |
|----------|-----------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Neon user |
| `SPRING_DATASOURCE_PASSWORD` | Neon password |
| `APP_JWT_SECRET` | Long random secret (32+ bytes) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` (recommended with Flyway) |
| `SPRING_JPA_SHOW_SQL` | `false` |

Optional bootstrap admin (only if you use these in the app):

- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
- `APP_BOOTSTRAP_ADMIN_FULL_NAME`

Render injects **`PORT`**; the app uses `server.port=${PORT:8080}` automatically.

## 4. Deploy Hook (GitHub Actions)

1. In Render: service **Settings → Deploy Hook → Create deploy hook**.
2. Copy the hook URL.
3. In GitHub: **Repository → Settings → Secrets and variables → Actions**, add:
   - `RENDER_DEPLOY_HOOK_URL` — the hook URL (used to trigger a deploy after CI passes).
   - `RENDER_HEALTHCHECK_URL` — public URL of the health endpoint, e.g. `https://<your-service>.onrender.com/actuator/health`.

## 5. Local Docker Compose

Copy `.env.example` to `.env`, adjust passwords and secrets, then:

```bash
docker compose up --build
```

API: `http://localhost:8080` — health: `http://localhost:8080/actuator/health`.

The same service also serves the React SPA from `/` (build is bundled into the JAR via Maven); the UI calls `/api/v1/...` on the same host.
