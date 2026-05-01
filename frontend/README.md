# Event System SPA (frontend)

## Требования

- Node.js **с npm** (обычная установка Node.js, не встроенный `node.exe` от Cursor)
- Запущенный backend Spring Boot на `http://localhost:8080`

## Запуск

```bash
cd frontend
npm install
npm run dev
```

По умолчанию dev-сервер проксирует запросы `/api/*` на `http://localhost:8080` (см. `vite.config.ts`), поэтому CORS не нужен.

## Страницы

- `/events` — CRUD + фильтры + отображение ManyToMany (категории) и OneToMany (tickets на странице события)
- `/tickets` — CRUD + фильтры `userId`/`barcode`
- `/users` — CRUD + find-by-email + OneToMany (tickets пользователя)
- `/organizers` — CRUD + search-by-name + OneToMany (events организатора)
- `/categories` — CRUD + filter-by-name + ManyToMany (events категории)

