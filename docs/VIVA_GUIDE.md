# Viva guide — BuildFlow concepts

Student-friendly explanations for SE2030 viva.  
**Udaththapola M D C · IT22101488**

---

## Spring Boot

Spring Boot is a Java framework that starts a web application with auto-configuration.  
In BuildFlow, `ConstructionManagementApplication` is the entry point. Embedded **Tomcat** serves HTTP on port **8080**. Dependencies (Web, Security, JPA, Thymeleaf, Flyway) are declared in `pom.xml`.

**Say in viva:** “Spring Boot reduces boilerplate so we focus on business modules instead of XML server setup.”

---

## MVC (Model–View–Controller)

| Part | In BuildFlow |
|------|----------------|
| **Model** | Entities + DTOs (forms) + data returned to templates |
| **View** | Thymeleaf HTML under `templates/` |
| **Controller** | `@Controller` classes map URLs and return view names |

Example flow: browser `GET /projects` → `ProjectController` → `ProjectService` → repository → Thymeleaf `projects/list`.

---

## Layered architecture

```text
Controller  →  Service  →  Repository  →  Database
     ↓             ↓
   DTO/Form     Entity/Business rules
```

- Controllers: HTTP, validation binding, redirects — thin
- Services: business rules (duplicate attendance, stock status, RBAC helpers)
- Repositories: Spring Data JPA interfaces
- Entities: tables mapped with JPA annotations

This separation keeps logic testable and clear for marking.

---

## JPA / Hibernate

**JPA** is the API; **Hibernate** is the implementation.  
`@Entity` classes map to tables. `@ManyToOne` / foreign keys model relationships (e.g. Task → Project).  
`spring.jpa.hibernate.ddl-auto=validate` means Hibernate checks the schema but **Flyway** creates it.

---

## PostgreSQL

Production database. Stores users, projects, materials, etc.  
Connected via JDBC URL `jdbc:postgresql://localhost:5432/construction_db`.  
**Flyway** runs `V1__initial_schema.sql` on startup.

---

## Spring Security

Protects every page except login/signup/setup and static files.  
Uses **form login** (`/login`), session after success, logout at `/logout`.  
`CustomUserDetailsService` loads users by email. Authorities are `ROLE_ADMIN`, `ROLE_CLIENT`, etc.

---

## BCrypt

Passwords are never stored as plain text.  
`BCryptPasswordEncoder` hashes on signup/setup; `matches()` checks login.  
In a dump of `app_users.password_hash` you only see a long hash string starting with `$2a$` / `$2b$`.

---

## RBAC (role-based access control)

Six roles: ADMIN, PROJECT_MANAGER, SITE_ENGINEER, CONTRACTOR, SUPPLIER, CLIENT.  
Method security: `@PreAuthorize("hasRole('ADMIN')")` or `hasAnyRole(...)`.  
Project visibility also filtered in services (e.g. clients see only their projects).

See [ROLE_ACCESS_MATRIX.md](ROLE_ACCESS_MATRIX.md).

---

## CRUD

Create, Read, Update, Delete for each module via HTML forms and controllers.  
Example: Materials — create item, list/search, edit, stock adjust, delete (Admin).

---

## Validation

Jakarta Bean Validation on DTOs: `@NotBlank`, `@Email`, `@Size`, `@DecimalMin`, `@AssertTrue`.  
Controllers use `@Valid` + `BindingResult`.  
Examples: signup password match; project end date not before start; material quantity ≥ 0.

---

## Relationships

- **Many-to-one:** many tasks belong to one project; many attendance rows to one worker  
- **Optional links:** project manager, material supplier, task assignee  
- Unique constraints: email, project_code, worker+date attendance

---

## BigDecimal

Used for money and stock (`budget`, `amount`, `current_quantity`, `unit_cost`).  
Avoids floating-point errors that `double` would cause for currency and inventory.

---

## Six modules (summary)

1. Projects & milestones  
2. Tasks  
3. Workers & attendance  
4. Materials, suppliers & requests  
5. Progress & expenses  
6. Issues (+ notifications)

---

## Frontend / backend / database

| Tier | Tech |
|------|------|
| Frontend | Thymeleaf server-rendered HTML, Bootstrap 5, Chart.js |
| Backend | Spring Boot services & security |
| Database | PostgreSQL + Flyway |

Not a separate SPA — server renders pages after each request (classic MVC).

---

## Tests

`@SpringBootTest` + `@ActiveProfiles("test")` uses **H2** in PostgreSQL compatibility mode with Flyway disabled and `ddl-auto=create-drop`, so exams/demos can run `./mvnw test` without Postgres.
