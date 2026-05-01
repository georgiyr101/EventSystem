-- Добавляет колонки auth для старых БД, где таблица users уже есть без role/password_hash/organizer_id.
-- На пустой БД пропускаем шаг: схему создаст Hibernate (ddl-auto=update).
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(32);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS organizer_id BIGINT;

        UPDATE users SET role = 'USER' WHERE role IS NULL;

        ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
        ALTER TABLE users ALTER COLUMN role SET NOT NULL;
    END IF;
END $$;
