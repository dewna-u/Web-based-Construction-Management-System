-- database/setup.sql
-- BuildFlow Construction Management System
-- Creates the PostgreSQL database, application role, and grants.
--
-- Run as a PostgreSQL superuser (e.g. postgres):
--   Windows (PowerShell):
--     $env:PGPASSWORD = "YOUR_POSTGRES_PASSWORD"
--     & "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost -f database\setup.sql
--   macOS:
--     psql -U postgres -f database/setup.sql
--
-- Defaults (must match application.properties):
--   Database : construction_db
--   User     : construction_user
--   Password : construction_pass
--
-- Tables are created later by Flyway when the app starts.
-- Prefer running the three commands below one at a time if \gexec is unavailable.

-- 1) Application role
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'construction_user') THEN
        CREATE ROLE construction_user LOGIN PASSWORD 'construction_pass';
    ELSE
        ALTER ROLE construction_user WITH LOGIN PASSWORD 'construction_pass';
    END IF;
END
$$;

-- 2) Database (psql only — \gexec). If this fails, run manually:
--    CREATE DATABASE construction_db OWNER construction_user;
SELECT 'CREATE DATABASE construction_db OWNER construction_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'construction_db')\gexec

-- 3) Schema privileges (requires construction_db to exist)
\c construction_db

GRANT USAGE, CREATE ON SCHEMA public TO construction_user;
ALTER SCHEMA public OWNER TO construction_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO construction_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO construction_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO construction_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO construction_user;
