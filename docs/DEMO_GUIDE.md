# Demo guide — BuildFlow

Use this script for a live demonstration (viva / marking).

**Student:** Udaththapola M D C · IT22101488 · SE2030

## Before you start

1. PostgreSQL running; `construction_db` created via `database/setup.sql`
2. App started: `.\mvnw.cmd spring-boot:run` or `./mvnw spring-boot:run`
3. Browser ready at [http://localhost:8080](http://localhost:8080)
4. `app.demo.enabled=true` (default in `application.properties`)

## Part A — Fresh start (2–3 minutes)

1. Open the app. If no users exist, you land on **`/setup`**.
2. Create Admin:
   - Name: e.g. `Demo Admin`
   - Email: e.g. `admin@buildflow.local`
   - Password: at least 8 characters
3. Log in → **Dashboard**.
4. As Admin, click **Generate demo data** (posts to `/admin/demo-data`).
5. Confirm success flash message.

## Part B — Core modules (8–10 minutes)

Walk through in this order:

| Step | Module | What to show |
|------|--------|----------------|
| 1 | Dashboard | Counts, charts, alerts (overdue tasks / low stock if present) |
| 2 | Projects | List → open a project → milestones / budget / progress |
| 3 | Tasks | Filter by status; show an overdue task if any |
| 4 | Workers | Worker list; open attendance and mark a day |
| 5 | Materials | Stock levels; adjust stock; show LOW_STOCK behaviour |
| 6 | Material requests | Create/approve flow (PM/Admin) |
| 7 | Progress | Add a progress update with percentage |
| 8 | Expenses | Add expense; show budget remaining on project |
| 9 | Issues | Report and resolve an issue |
| 10 | Users (Admin) | Create a SITE_ENGINEER or PROJECT_MANAGER |

## Part C — Roles (3–4 minutes)

1. **Signup** a CLIENT or CONTRACTOR at `/signup`.
2. Log in as that user — show restricted menus / project visibility.
3. Log back in as Admin — show full access.

## Part D — Security talking points

- Passwords stored as **BCrypt** hashes (not plaintext)
- Form login + session cookie
- `@PreAuthorize` on mutating endpoints
- Public routes: `/login`, `/signup`, `/setup`, static assets

## Part E — If demo data already exists

- Skip generate, or reset DB (see README → Reset database), re-setup, then generate again.

## Timing tip

Aim for **15 minutes** total: setup + demo data + projects/tasks/materials + one role switch.
