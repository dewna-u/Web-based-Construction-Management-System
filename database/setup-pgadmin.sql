-- database/setup-pgadmin.sql
-- Use this in pgAdmin Query Tool (connect to database "postgres" as superuser).
-- Run each section separately if your pgAdmin version does not support multi-statement scripts.

-- 1) Create role (ignore error if it already exists)
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

-- 2) Create database (run once; skip if construction_db already exists)
-- In pgAdmin: right-click Databases → Create → Database → Name: construction_db → Owner: construction_user
-- Or execute (cannot run inside a DO block in older versions):
SELECT 'Run manually if needed: CREATE DATABASE construction_db OWNER construction_user;' AS note;

-- Prefer creating via pgAdmin UI, then connect to construction_db and run:

GRANT CONNECT ON DATABASE construction_db TO construction_user;

-- 3) Connect to construction_db in pgAdmin, then run:
GRANT USAGE, CREATE ON SCHEMA public TO construction_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO construction_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO construction_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO construction_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO construction_user;
