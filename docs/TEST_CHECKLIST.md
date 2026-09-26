# Manual test checklist — BuildFlow

Use beside automated `./mvnw test`. Mark each item during local QA.

## Environment

- [ ] JDK 21 installed (`java -version`)
- [ ] PostgreSQL running on 5432
- [ ] `database/setup.sql` executed successfully
- [ ] `.\mvnw.cmd spring-boot:run` / `./mvnw spring-boot:run` starts without errors
- [ ] Flyway applied V1 (tables visible in pgAdmin)

## Setup / auth

- [ ] Fresh DB redirects to `/setup`
- [ ] Admin created; redirect to login
- [ ] Invalid login shows error
- [ ] Valid login → `/dashboard`
- [ ] Logout → login with message
- [ ] Signup CLIENT works
- [ ] Signup CONTRACTOR works
- [ ] Signup rejects ADMIN role (validation)
- [ ] Duplicate email rejected
- [ ] Second visit to `/setup` redirects to login when users exist

## Projects

- [ ] Admin/PM can create project with CLIENT
- [ ] Duplicate project code rejected
- [ ] End date before start rejected
- [ ] Client sees only own projects
- [ ] Archive / unarchive works

## Tasks

- [ ] Create task on project
- [ ] Assign user
- [ ] Past due + not COMPLETED shows overdue
- [ ] Complete task clears overdue

## Workers / attendance

- [ ] Create worker
- [ ] Mark attendance
- [ ] Duplicate same worker+date rejected
- [ ] Daily summary counts update

## Materials

- [ ] Create material with reorder level
- [ ] Reduce stock to ≤ reorder → LOW_STOCK
- [ ] Reduce to 0 → OUT_OF_STOCK
- [ ] Adjust below zero rejected
- [ ] Material request approve/reject (PM/Admin)

## Progress / expenses / issues

- [ ] Progress update changes project %
- [ ] Expense reduces budget remaining
- [ ] Issue report + resolve

## Security

- [ ] Unauthenticated `/dashboard` redirects to login
- [ ] Contractor cannot open Admin users
- [ ] Supplier cannot manage projects (403)

## Demo

- [ ] Admin generates demo data once
- [ ] Second generate does not duplicate incorrectly

## Automated tests

- [ ] `.\mvnw.cmd test` / `./mvnw test` all green
