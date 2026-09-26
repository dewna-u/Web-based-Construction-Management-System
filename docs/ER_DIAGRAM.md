# ER diagram — BuildFlow

Mermaid entity-relationship diagram (compatible with GitHub / many Markdown previewers).

```mermaid
erDiagram
    APP_USERS ||--o{ PROJECTS : "client_of"
    APP_USERS ||--o{ PROJECTS : "manages"
    APP_USERS ||--o{ TASKS : "assigned_to"
    APP_USERS ||--o{ WORKERS : "linked_user"
    APP_USERS ||--o{ ATTENDANCE_RECORDS : "recorded_by"
    APP_USERS ||--o{ SUPPLIERS : "linked_user"
    APP_USERS ||--o{ MATERIAL_REQUESTS : "requests"
    APP_USERS ||--o{ MATERIAL_REQUESTS : "reviews"
    APP_USERS ||--o{ EXPENSES : "recorded_by"
    APP_USERS ||--o{ PROGRESS_UPDATES : "created_by"
    APP_USERS ||--o{ ISSUES : "reported_by"
    APP_USERS ||--o{ ISSUES : "assigned_to"
    APP_USERS ||--o{ NOTIFICATIONS : "receives"

    PROJECTS ||--o{ MILESTONES : "has"
    PROJECTS ||--o{ TASKS : "has"
    PROJECTS ||--o{ WORKERS : "assigned"
    PROJECTS ||--o{ MATERIAL_REQUESTS : "has"
    PROJECTS ||--o{ EXPENSES : "has"
    PROJECTS ||--o{ PROGRESS_UPDATES : "has"
    PROJECTS ||--o{ ISSUES : "has"

    WORKERS ||--o{ ATTENDANCE_RECORDS : "has"
    SUPPLIERS ||--o{ MATERIALS : "supplies"
    MATERIALS ||--o{ MATERIAL_REQUESTS : "requested_in"

    APP_USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
    }

    PROJECTS {
        bigint id PK
        varchar name
        varchar project_code UK
        bigint client_id FK
        bigint manager_id FK
        varchar status
        numeric budget
        int progress_percentage
        boolean archived
    }

    MILESTONES {
        bigint id PK
        bigint project_id FK
        varchar title
        varchar status
        date due_date
    }

    TASKS {
        bigint id PK
        bigint project_id FK
        bigint assignee_id FK
        varchar title
        varchar priority
        varchar status
        date due_date
    }

    WORKERS {
        bigint id PK
        varchar worker_code UK
        varchar full_name
        bigint assigned_project_id FK
        bigint linked_user_id FK
        varchar status
    }

    ATTENDANCE_RECORDS {
        bigint id PK
        bigint worker_id FK
        date attendance_date
        varchar status
        bigint recorded_by_id FK
    }

    SUPPLIERS {
        bigint id PK
        varchar name
        boolean active
        bigint linked_user_id FK
    }

    MATERIALS {
        bigint id PK
        varchar material_code UK
        numeric current_quantity
        numeric reorder_level
        bigint supplier_id FK
        varchar status
    }

    MATERIAL_REQUESTS {
        bigint id PK
        bigint project_id FK
        bigint material_id FK
        bigint requester_id FK
        numeric quantity
        varchar status
    }

    EXPENSES {
        bigint id PK
        bigint project_id FK
        varchar category
        numeric amount
        date expense_date
    }

    PROGRESS_UPDATES {
        bigint id PK
        bigint project_id FK
        int progress_percentage
        text summary
        varchar image_path
    }

    ISSUES {
        bigint id PK
        bigint project_id FK
        bigint reported_by_id FK
        bigint assigned_to_id FK
        varchar severity
        varchar status
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar title
        boolean read_flag
    }
```

## Cardinality notes

- One CLIENT user can own many projects (`client_id`).
- One worker may have many attendance rows, but **at most one per calendar day** (unique constraint).
- Materials optionally reference one supplier; many materials can share a supplier.
