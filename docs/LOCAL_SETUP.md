# Local setup guide — BuildFlow

Student: **Udaththapola M D C** · IT22101488 · SE2030

This guide walks through a clean install on Windows or macOS. **Docker is not used.**

## 1. Prerequisites

- JDK **21**
- PostgreSQL **14+** (port **5432**)
- Optional: pgAdmin 4
- Git (optional)

Verify Java:

```bash
java -version
```

## 2. Clone / open the project

Open the folder that contains `pom.xml` in your IDE or terminal.

## 3. Create the database

Run as PostgreSQL superuser:

```bash
psql -U postgres -f database/setup.sql
```

Creates:

- Database: `construction_db`
- Role: `construction_user` / `construction_pass`
- Schema privileges for Flyway

## 4. Configuration check

Defaults in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/construction_db
spring.datasource.username=construction_user
spring.datasource.password=construction_pass
server.port=8080
```

Change only if your local Postgres differs.

## 5. Run the application

**Windows**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

First run downloads Maven 3.9.9 and dependencies.

## 6. First-time setup

1. Browser → [http://localhost:8080](http://localhost:8080)
2. Complete **`/setup`** (first admin)
3. Log in at **`/login`**
4. Optional: generate demo data (Admin dashboard)

## 7. Run tests

Tests use in-memory **H2** (`application-test.properties`). PostgreSQL is **not** required for tests.

```bash
./mvnw test
# Windows:
.\mvnw.cmd test
```

## 8. Uploads folder

Progress images and uploads go to `uploads/` (created at runtime). It is gitignored.

## Common failures

| Symptom | Fix |
|---------|-----|
| Auth failed for user | Re-run `setup.sql`; confirm password |
| Flyway checksum / failed migration | Drop and recreate `construction_db` |
| Address already in use | Free port 8080 or change `server.port` |
| Wrapper cannot download Maven | Check firewall/proxy; retry |

See also [README.md](../README.md) and [DEMO_GUIDE.md](DEMO_GUIDE.md).
