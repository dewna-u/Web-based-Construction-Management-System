# Role access matrix — BuildFlow

Roles: **ADMIN**, **PROJECT_MANAGER (PM)**, **SITE_ENGINEER (SE)**, **CONTRACTOR (CT)**, **SUPPLIER (SU)**, **CLIENT (CL)**

Legend: **F** = full CRUD / manage · **R** = read / limited write · **—** = no access (typically 403) · **Own** = scoped to own projects/assignments

| Feature / endpoint area | ADMIN | PM | SE | CT | SU | CL |
|-------------------------|:-----:|:--:|:--:|:--:|:--:|:--:|
| Dashboard | F | F | F | F | R | F |
| User admin (`/admin/users`) | F | — | — | — | — | — |
| Demo data (`/admin/demo-data`) | F | — | — | — | — | — |
| Projects list/view | F | F | Own | Own | — | Own |
| Projects create/edit/archive | F | F | — | — | — | — |
| Milestones create/edit | F | F | F | — | — | — |
| Milestones delete | F | F | — | — | — | — |
| Tasks create/edit | F | F | F | — | — | — |
| Task status (contractor) | F | F | F | R | — | — |
| Tasks delete | F | F | — | — | — | — |
| Workers CRUD | F | F | — | — | — | — |
| Workers delete | F | — | — | — | — | — |
| Attendance create/edit | F | F | F | — | — | — |
| Attendance delete | F | F | — | — | — | — |
| Suppliers manage | F | F | — | — | — | — |
| Suppliers delete | F | — | — | — | — | — |
| Materials CRUD | F | F | — | — | — | — |
| Stock adjustment | F | F | F | — | — | — |
| Materials delete | F | — | — | — | — | — |
| Material requests create | F | F | F | F | — | — |
| Material requests approve/reject | F | F | — | — | — | — |
| Expenses | F | F | — | — | — | — |
| Progress updates create/edit | F | F | F | — | — | — |
| Progress delete | F | F | — | — | — | — |
| Issues report/view | F | F | F | F | — | F |
| Issues assign/resolve | F | F | F* | — | — | — |
| Profile / change password | F | F | F | F | F | F |
| Public signup (CLIENT/CONTRACTOR) | n/a | n/a | n/a | n/a | n/a | n/a |

\*SE can update issues within allowed `@PreAuthorize` rules; PM/Admin have broader control.

## Notes

- Method-level security uses `@PreAuthorize` / `@EnableMethodSecurity`.
- Unauthenticated users only reach `/login`, `/signup`, `/setup`, and static assets.
- Project visibility for CLIENT/SE/CONTRACTOR is further limited in `ProjectService.canAccess`.
- SUPPLIER access is intentionally narrow in this version.

Derived from controller annotations in `com.buildflow.cms.controller`.
