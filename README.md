# BuildFlow — Construction Management System

**Student:** Udaththapola M D C  
**Student ID:** IT22101488  
**Module:** SE2030  

BuildFlow is a web-based Construction Management System for coordinating construction projects, site tasks, workers, materials, progress, expenses, and issues. It uses Spring Boot, Thymeleaf, PostgreSQL, and role-based access control (RBAC).

---

## Tech stack

| Layer | Technology |
|--------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Web UI | Thymeleaf + Bootstrap 5 + Bootstrap Icons + Chart.js |
| Security | Spring Security (form login, BCrypt, method security) |
| Persistence | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Database | PostgreSQL |
| Build | Maven (wrapper included) |
| Tests | JUnit 5, Spring Boot Test, Spring Security Test, H2 |

**No Docker** is required or provided for this project.

---

## System requirements

- **Java 21** (JDK) or newer
- **PostgreSQL 14+** (16 recommended)
- **pgAdmin 4** (optional GUI)
- Internet access on first Maven run (to download dependencies)
- ~2 GB free disk space for Maven cache + database

---

## Java installation

### Windows

1. Download Temurin JDK 21 from [Adoptium](https://adoptium.net/).
2. Run the installer and enable **Set JAVA_HOME**.
3. Open a **new** PowerShell or Command Prompt and verify:

```powershell
java -version
```

You should see a version line containing `21`.

### macOS

Using Homebrew:

```bash
brew install temurin@21
java -version
```

Or download the macOS Temurin installer from Adoptium and follow the wizard.

---

## PostgreSQL setup

### Windows

1. Download PostgreSQL from [postgresql.org/download/windows](https://www.postgresql.org/download/windows/).
2. Install with the default port **5432**.
3. Remember the password you set for the `postgres` superuser.
4. Ensure the PostgreSQL service is running (Services app → postgresql-x64-…).

### macOS

```bash
brew install postgresql@16
brew services start postgresql@16
```

---

## pgAdmin

1. Install [pgAdmin 4](https://www.pgadmin.org/download/) if it was not bundled with PostgreSQL.
2. Open pgAdmin and connect to the local server (`localhost`, user `postgres`).
3. You can run `database/setup.sql` from Query Tool, or use `psql` as shown below.

---

## Database creation

From a terminal (as the PostgreSQL superuser):

### Windows (psql)

```powershell
psql -U postgres -f "database\setup.sql"
```

### macOS / Linux

```bash
psql -U postgres -f database/setup.sql
```

This script creates:

| Item | Value |
|------|--------|
| Database | `construction_db` |
| User | `construction_user` |
| Password | `construction_pass` |

It also grants schema privileges so Flyway can create tables on first application start.

**Manual alternative (pgAdmin Query Tool):** open and execute `database/setup.sql`.

---

## Configuration

Default settings live in `src/main/resources/application.properties`:

| Property | Default |
|----------|---------|
| `server.port` | `8080` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/construction_db` |
| `spring.datasource.username` | `construction_user` |
| `spring.datasource.password` | `construction_pass` |
| `app.upload.dir` | `uploads` |
| `app.demo.enabled` | `true` |

Override with environment variables if needed:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/construction_db"
$env:DB_USERNAME="construction_user"
$env:DB_PASSWORD="construction_pass"
$env:UPLOAD_DIR="uploads"
$env:DEMO_ENABLED="true"
```

Flyway runs migration `V1__initial_schema.sql` automatically on startup. Hibernate is set to `validate` (schema is owned by Flyway, not auto-DDL).

---

## How to run on Windows

1. Complete Java + PostgreSQL + `database/setup.sql`.
2. Open PowerShell in the project root (folder containing `pom.xml`).
3. Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Wait until you see Spring Boot started.
5. Open a browser: [http://localhost:8080](http://localhost:8080)

First Maven run downloads dependencies and may take several minutes.

---

## How to run on macOS

1. Complete Java + PostgreSQL + `database/setup.sql`.
2. Make the wrapper executable (once):

```bash
chmod +x mvnw
```

3. Start the app:

```bash
./mvnw spring-boot:run
```

4. Open [http://localhost:8080](http://localhost:8080)

---

## Maven wrapper commands

```text
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows

./mvnw test                     # run unit/integration tests
./mvnw -DskipTests package      # build JAR without tests
```

The wrapper downloads **Maven 3.9.9** automatically — you do not need a global Maven install.

---

## First start — `/setup`

On a fresh database with **no users**, visiting the app redirects you to **`/setup`**.

1. Enter admin full name, email, and password (min 8 characters).
2. Submit to create the first **ADMIN** account.
3. You are redirected to `/login`.

After an admin exists, `/setup` is no longer available for creating another first admin.

---

## Login

- URL: [http://localhost:8080/login](http://localhost:8080/login)
- Use the admin email/password from setup, or any later account.
- Successful login goes to `/dashboard`.

---

## Signup

- URL: [http://localhost:8080/signup](http://localhost:8080/signup)
- Public self-registration is limited to **CLIENT** or **CONTRACTOR**.
- Other roles (Project Manager, Site Engineer, Supplier, Admin) are created by an Admin under user management.

---

## Role descriptions

| Role | Purpose |
|------|---------|
| **ADMIN** | Full system control: users, all modules, demo data, deletes |
| **PROJECT_MANAGER** | Create/manage projects, tasks, workers, materials, expenses, progress, issues |
| **SITE_ENGINEER** | Site operations: tasks, attendance, progress, issues, material requests, stock view |
| **CONTRACTOR** | Assigned work: view tasks, raise material requests, report issues |
| **SUPPLIER** | Limited supplier-facing views (linked supplier account) |
| **CLIENT** | View own projects, progress, and issues |

Access is enforced with Spring Security + `@PreAuthorize` on controllers. See [docs/ROLE_ACCESS_MATRIX.md](docs/ROLE_ACCESS_MATRIX.md).

---

## Module descriptions

BuildFlow is organised around six core construction modules (plus supporting features):

1. **Projects & milestones** — project lifecycle, budget, progress %, milestones  
2. **Tasks** — assignable work items with priority, due dates, overdue detection  
3. **Workforce & attendance** — workers, daily attendance (unique per worker/day)  
4. **Materials & suppliers** — inventory, reorder levels, stock adjustments, requests  
5. **Progress & expenses** — site progress updates (with optional images) and cost tracking  
6. **Issues & notifications** — site issues by severity/status; in-app notifications  

Supporting: **Dashboard**, **Admin users**, **Profile**, optional **Demo data**.

Details: [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md), [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md).

---

## Database table summary

| Table | Purpose |
|-------|---------|
| `app_users` | Accounts, roles, BCrypt password hashes |
| `projects` | Construction projects |
| `milestones` | Project milestones |
| `tasks` | Task items |
| `workers` | Site workers |
| `attendance_records` | Daily attendance |
| `suppliers` | Material suppliers |
| `materials` | Inventory items |
| `material_requests` | Site material requests |
| `expenses` | Project expenses |
| `progress_updates` | Progress logs |
| `issues` | Project issues |
| `notifications` | User notifications |

Full schema: [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) · ER diagram: [docs/ER_DIAGRAM.md](docs/ER_DIAGRAM.md).

---

## Project structure

```text
├── database/setup.sql
├── docs/                          # setup, demo, viva, schema, tests
├── mvnw / mvnw.cmd                # Maven wrapper
├── .mvn/wrapper/
├── pom.xml
├── src/main/java/com/buildflow/cms/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── security/
│   ├── config/
│   ├── enums/
│   └── exception/
├── src/main/resources/
│   ├── application.properties
│   ├── db/migration/
│   ├── static/
│   └── templates/
└── src/test/                      # H2-backed Spring Boot tests
```

More detail: [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md).

---

## Troubleshooting

| Problem | What to try |
|---------|-------------|
| `Connection refused` / cannot connect to DB | Start PostgreSQL; confirm port 5432; check username/password |
| `password authentication failed` | Re-run `database/setup.sql` or reset `construction_user` password |
| `relation does not exist` | Flyway did not run — check logs; ensure empty DB and restart app |
| Port 8080 in use | Stop the other process or set `server.port=8081` |
| `JAVA_HOME` / wrong Java | Install JDK 21; open a new terminal; `java -version` |
| Maven wrapper fails to download | Check internet/proxy; retry `.\mvnw.cmd -v` |
| Upload errors | Ensure `uploads/` is writable; check `app.upload.dir` |
| Redirected to login unexpectedly | Session expired — sign in again |

More: [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md).

---

## Reset database

**Destructive** — deletes all application data.

```sql
-- as postgres superuser
DROP DATABASE IF EXISTS construction_db;
```

Then re-run `database/setup.sql` and start the app again. Visit `/setup` to create a new admin.

In pgAdmin: disconnect clients → drop database → recreate via `setup.sql`.

---

## Demo procedure

1. Start PostgreSQL and the app (`.\mvnw.cmd spring-boot:run` or `./mvnw spring-boot:run`).
2. Open [http://localhost:8080](http://localhost:8080) → complete `/setup` if needed → log in as Admin.
3. Ensure `app.demo.enabled=true` (default).
4. From the dashboard (Admin), trigger **Generate demo data** (`POST /admin/demo-data`).
5. Explore projects, tasks, workers, materials, expenses, progress, and issues.
6. Optionally log out and sign in as demo CLIENT/CONTRACTOR accounts if created by the demo service.

Step-by-step viva/demo script: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md).

---

## Documentation index

| Document | Description |
|----------|-------------|
| [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md) | Detailed local setup |
| [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md) | Demo walkthrough |
| [docs/VIVA_GUIDE.md](docs/VIVA_GUIDE.md) | Concepts for viva |
| [docs/ROLE_ACCESS_MATRIX.md](docs/ROLE_ACCESS_MATRIX.md) | RBAC matrix |
| [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) | Tables & columns |
| [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | Package layout |
| [docs/TEST_CHECKLIST.md](docs/TEST_CHECKLIST.md) | Manual test checklist |
| [docs/ER_DIAGRAM.md](docs/ER_DIAGRAM.md) | Mermaid ER diagram |

---

## License / academic use

Submitted for **SE2030** coursework. Not licensed for production deployment without further hardening.
