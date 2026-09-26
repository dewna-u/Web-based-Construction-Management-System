# Project structure — BuildFlow

```text
Web-based Construction Management System/
├── README.md
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/
│   ├── maven-wrapper.jar
│   └── maven-wrapper.properties          # Maven 3.9.9
├── .gitignore
├── database/
│   └── setup.sql                         # DB + role bootstrap
├── docs/                                 # Academic / setup docs
│   ├── LOCAL_SETUP.md
│   ├── DEMO_GUIDE.md
│   ├── VIVA_GUIDE.md
│   ├── ROLE_ACCESS_MATRIX.md
│   ├── DATABASE_SCHEMA.md
│   ├── PROJECT_STRUCTURE.md
│   ├── TEST_CHECKLIST.md
│   └── ER_DIAGRAM.md
└── src/
    ├── main/
    │   ├── java/com/buildflow/cms/
    │   │   ├── ConstructionManagementApplication.java
    │   │   ├── config/                   # MVC, setup interceptor
    │   │   ├── controller/               # HTTP endpoints
    │   │   ├── dto/                      # Form objects + validation
    │   │   ├── entity/                   # JPA entities
    │   │   ├── enums/                    # Roles, statuses, etc.
    │   │   ├── exception/                # Business + global handler
    │   │   ├── repository/               # Spring Data JPA
    │   │   ├── security/                 # SecurityConfig, UserDetails
    │   │   ├── service/                  # Business logic
    │   │   └── util/                     # SecurityUtils
    │   └── resources/
    │       ├── application.properties
    │       ├── db/migration/
    │       │   └── V1__initial_schema.sql
    │       ├── static/
    │       │   ├── css/ / js/ (if present)
    │       │   └── images/
    │       │       ├── ATTRIBUTION.md
    │       │       └── hero-construction.svg
    │       └── templates/                # Thymeleaf views
    └── test/
        ├── java/com/buildflow/cms/
        │   ├── service/                  # Service integration tests
        │   ├── security/                 # Security / MockMvc tests
        │   └── validation/               # Bean Validation tests
        └── resources/
            └── application-test.properties   # H2, Flyway off
```

## Package responsibilities

| Package | Responsibility |
|---------|----------------|
| `controller` | Map URLs, bind forms, choose views |
| `service` | Business rules and transactions |
| `repository` | Database queries |
| `entity` | Persistent domain model |
| `dto` | Input forms with validation annotations |
| `security` | Authn/authz configuration |
| `config` | Web MVC extras (e.g. force `/setup`) |
| `exception` | Domain errors + `@ControllerAdvice` |

## Build artifact

```bash
./mvnw -DskipTests package
# → target/construction-management-system-1.0.0.jar
```
