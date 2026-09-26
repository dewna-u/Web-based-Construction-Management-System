# Database schema — BuildFlow

Source of truth: `src/main/resources/db/migration/V1__initial_schema.sql`  
DB name: **construction_db** · Owner app role: **construction_user**

## Tables

### app_users
| Column | Type | Notes |
|--------|------|--------|
| id | BIGSERIAL PK | |
| full_name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(150) | UNIQUE |
| password_hash | VARCHAR(255) | BCrypt |
| role | VARCHAR(50) | enum name |
| enabled | BOOLEAN | default true |
| created_at / updated_at | TIMESTAMP | |

### projects
| Column | Type | Notes |
|--------|------|--------|
| id | BIGSERIAL PK | |
| name | VARCHAR(200) | |
| project_code | VARCHAR(50) | UNIQUE |
| description | TEXT | |
| location | VARCHAR(255) | |
| client_id | FK → app_users | NOT NULL |
| manager_id | FK → app_users | nullable |
| start_date / expected_end_date / actual_end_date | DATE | |
| status | VARCHAR(50) | default PLANNING |
| budget | NUMERIC(15,2) | |
| progress_percentage | INTEGER | |
| archived | BOOLEAN | |
| created_at / updated_at | TIMESTAMP | |

### milestones
FK `project_id` → projects. Status, due/completion dates.

### tasks
FK `project_id` → projects, optional `assignee_id` → app_users. Priority, status, completion %.

### workers
Unique `worker_code`. Optional `assigned_project_id`, `linked_user_id`. Employment type, daily rate, status.

### attendance_records
FK `worker_id`. Unique `(worker_id, attendance_date)`. Optional `recorded_by_id`.

### suppliers
Contact fields; optional `linked_user_id`; `active` flag.

### materials
Unique `material_code`. Quantity, reorder level, unit cost (NUMERIC). Optional `supplier_id`. Status (AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED).

### material_requests
FKs: project, material, requester; optional reviewer. Status workflow PENDING → APPROVED/REJECTED/etc.

### expenses
FK project; category; amount NUMERIC(15,2); expense_date; optional recorded_by.

### progress_updates
FK project; progress %; summary; optional image_path; created_by.

### issues
FK project; reported_by; optional assigned_to; severity; status; resolution fields.

### notifications
FK user_id; title; message; link; type; read_flag.

## Indexes

Created on foreign keys and common filters (status, attendance date, read_flag, etc.).

## Setup script

`database/setup.sql` creates the database and role and grants privileges. It does **not** create tables — Flyway does that on app start.

## ER diagram

See [ER_DIAGRAM.md](ER_DIAGRAM.md).
