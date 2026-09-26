-- V1__initial_schema.sql
-- BuildFlow Construction Management System — initial PostgreSQL schema

CREATE TABLE app_users (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE projects (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(200)   NOT NULL,
    project_code          VARCHAR(50)    NOT NULL UNIQUE,
    description           TEXT,
    location              VARCHAR(255),
    client_id             BIGINT         NOT NULL REFERENCES app_users (id),
    manager_id            BIGINT         REFERENCES app_users (id),
    start_date            DATE,
    expected_end_date     DATE,
    actual_end_date       DATE,
    status                VARCHAR(50)    NOT NULL DEFAULT 'PLANNING',
    budget                NUMERIC(15, 2),
    progress_percentage   INTEGER        DEFAULT 0,
    archived              BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_client_id ON projects (client_id);
CREATE INDEX idx_projects_manager_id ON projects (manager_id);
CREATE INDEX idx_projects_status ON projects (status);

CREATE TABLE milestones (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    project_id        BIGINT       NOT NULL REFERENCES projects (id),
    due_date          DATE,
    completion_date   DATE,
    status            VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_milestones_project_id ON milestones (project_id);

CREATE TABLE tasks (
    id                      BIGSERIAL PRIMARY KEY,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT,
    project_id              BIGINT       NOT NULL REFERENCES projects (id),
    assignee_id             BIGINT       REFERENCES app_users (id),
    priority                VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM',
    start_date              DATE,
    due_date                DATE,
    status                  VARCHAR(50)  NOT NULL DEFAULT 'TODO',
    completion_percentage   INTEGER      DEFAULT 0,
    notes                   TEXT,
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_project_id ON tasks (project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
CREATE INDEX idx_tasks_status ON tasks (status);

CREATE TABLE workers (
    id                    BIGSERIAL PRIMARY KEY,
    worker_code           VARCHAR(50)    NOT NULL UNIQUE,
    full_name             VARCHAR(150)   NOT NULL,
    nic                   VARCHAR(50),
    phone                 VARCHAR(30),
    email                 VARCHAR(150),
    trade_skill           VARCHAR(100),
    employment_type       VARCHAR(50)    NOT NULL,
    daily_rate            NUMERIC(12, 2),
    status                VARCHAR(50)    NOT NULL DEFAULT 'ACTIVE',
    assigned_project_id   BIGINT         REFERENCES projects (id),
    linked_user_id        BIGINT         REFERENCES app_users (id),
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workers_assigned_project_id ON workers (assigned_project_id);
CREATE INDEX idx_workers_status ON workers (status);

CREATE TABLE attendance_records (
    id                BIGSERIAL PRIMARY KEY,
    worker_id         BIGINT      NOT NULL REFERENCES workers (id),
    attendance_date   DATE        NOT NULL,
    status            VARCHAR(50) NOT NULL,
    notes             TEXT,
    recorded_by_id    BIGINT      REFERENCES app_users (id),
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_worker_date UNIQUE (worker_id, attendance_date)
);

CREATE INDEX idx_attendance_records_worker_id ON attendance_records (worker_id);
CREATE INDEX idx_attendance_records_date ON attendance_records (attendance_date);

CREATE TABLE suppliers (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    contact_person   VARCHAR(150),
    phone            VARCHAR(30),
    email            VARCHAR(150),
    address          TEXT,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    linked_user_id   BIGINT       REFERENCES app_users (id),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE materials (
    id                  BIGSERIAL PRIMARY KEY,
    material_code       VARCHAR(50)    NOT NULL UNIQUE,
    name                VARCHAR(200)   NOT NULL,
    category            VARCHAR(100),
    unit                VARCHAR(50)    NOT NULL,
    current_quantity    NUMERIC(15, 3) NOT NULL DEFAULT 0,
    reorder_level       NUMERIC(15, 3),
    unit_cost           NUMERIC(15, 2),
    supplier_id         BIGINT         REFERENCES suppliers (id),
    storage_location    VARCHAR(150),
    status              VARCHAR(50)    NOT NULL DEFAULT 'AVAILABLE',
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_materials_supplier_id ON materials (supplier_id);
CREATE INDEX idx_materials_status ON materials (status);

CREATE TABLE material_requests (
    id                 BIGSERIAL PRIMARY KEY,
    project_id         BIGINT         NOT NULL REFERENCES projects (id),
    material_id        BIGINT         NOT NULL REFERENCES materials (id),
    requester_id       BIGINT         NOT NULL REFERENCES app_users (id),
    quantity           NUMERIC(15, 3) NOT NULL,
    request_date       DATE           NOT NULL,
    required_date      DATE,
    status             VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    notes              TEXT,
    rejection_reason   TEXT,
    reviewed_by_id     BIGINT         REFERENCES app_users (id),
    reviewed_at        TIMESTAMP,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_material_requests_project_id ON material_requests (project_id);
CREATE INDEX idx_material_requests_status ON material_requests (status);

CREATE TABLE expenses (
    id               BIGSERIAL PRIMARY KEY,
    project_id       BIGINT         NOT NULL REFERENCES projects (id),
    category         VARCHAR(50)    NOT NULL,
    description      VARCHAR(255)   NOT NULL,
    amount           NUMERIC(15, 2) NOT NULL,
    expense_date     DATE           NOT NULL,
    reference        VARCHAR(100),
    notes            TEXT,
    recorded_by_id   BIGINT         REFERENCES app_users (id),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_project_id ON expenses (project_id);
CREATE INDEX idx_expenses_category ON expenses (category);

CREATE TABLE progress_updates (
    id                      BIGSERIAL PRIMARY KEY,
    project_id              BIGINT      NOT NULL REFERENCES projects (id),
    update_date             DATE        NOT NULL,
    progress_percentage     INTEGER     NOT NULL,
    summary                 TEXT        NOT NULL,
    work_completed          TEXT,
    next_planned_work       TEXT,
    created_by_id           BIGINT      REFERENCES app_users (id),
    image_path              VARCHAR(500),
    created_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_progress_updates_project_id ON progress_updates (project_id);

CREATE TABLE issues (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT       NOT NULL REFERENCES projects (id),
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    reported_by_id      BIGINT       NOT NULL REFERENCES app_users (id),
    assigned_to_id      BIGINT       REFERENCES app_users (id),
    severity            VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM',
    status              VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    reported_date       DATE         NOT NULL,
    resolved_date       DATE,
    resolution_notes    TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_issues_project_id ON issues (project_id);
CREATE INDEX idx_issues_status ON issues (status);
CREATE INDEX idx_issues_severity ON issues (severity);

CREATE TABLE notifications (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES app_users (id),
    title        VARCHAR(200) NOT NULL,
    message      TEXT         NOT NULL,
    link         VARCHAR(500),
    type         VARCHAR(50)  NOT NULL DEFAULT 'GENERAL',
    read_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_read_flag ON notifications (user_id, read_flag);
