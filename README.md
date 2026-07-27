# blog-cms

A modern, self-hostable, open-source Blog CMS built with Java Spring Boot and React.

Designed to work as a classic blog platform or as a headless CMS — clean REST API, role-based access control, and an extensible open-core architecture.

---

## Features

- **Content management** – create, edit, publish, and archive posts with a full status lifecycle (Draft → Review → Published → Archived)
- **Markdown-based content** – posts are stored in Markdown, rendering is handled by the frontend
- **Categories & Tags** – organize content with a flexible taxonomy
- **Media uploads** – upload and manage images and files
- **Role-based access** – three roles out of the box: `ADMIN`, `EDITOR`, `AUTHOR`
- **JWT Authentication** – stateless auth with access + refresh token flow
- **Public REST API** – consume content from any frontend or external client
- **Admin REST API** – fully authenticated management endpoints
- **Self-hostable** – runs on any machine with Java and PostgreSQL; Docker support included

---

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

**Frontend**
- React 19
- TypeScript
- Vite
- React Router
- TanStack Query

---

## Getting Started

### Prerequisites

- Java 21+
- Maven
- PostgreSQL
- Node.js 18+

---

### Backend

1. Clone the repository

```bash
git clone https://github.com/your-username/blog-cms.git
cd blog-cms/backend
```

2. Copy the example environment file and configure it

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

3. Set your database connection in `application-local.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogcms
    username: your_user
    password: your_password

app:
  bootstrap:
    enabled: true
    admin:
      username: admin
      email: admin@example.com
      password: changeme
```

4. Start the backend

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.
An initial admin user will be created automatically on first start.

---

### Frontend

```bash
cd blog-cms/frontend
npm install
npm run dev
```

The frontend is available at `http://localhost:5173`.

---

## Project Structure

```
blog-cms/
├── backend/
│   └── src/main/java/de/patrick/schmidt/blogcms/
│       ├── auth/
│       ├── user/
│       ├── post/
│       ├── category/
│       ├── tag/
│       ├── media/
│       ├── common/
│       └── config/
├── frontend/
│   └── src/
├── docs/
│   └── adr/              # Architecture Decision Records
└── CONTRIBUTING.md
```

The backend follows a **package-by-feature** architecture, not a layered architecture. Each module owns its controller, service, repository, and domain models.

---

## API Overview

```
Public API  →  /api/public/v1/...   (no auth required)
Admin API   →  /api/admin/v1/...    (JWT required)
Auth        →  /api/auth/...
```

Full API documentation is available via the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

---

## Architecture Decisions

All significant design decisions are documented as Architecture Decision Records (ADRs) under [`docs/adr/`](docs/adr/).

| ADR | Topic |
|-----|-------|
| [ADR-001](docs/adr/ADR-001.md) | Project vision & scope |
| [ADR-002](docs/adr/ADR-002.md) | Identifiers & auditing |
| [ADR-003](docs/adr/ADR-003.md) | Domain model & post lifecycle |
| [ADR-004](docs/adr/ADR-004.md) | REST API contract |
| [ADR-005](docs/adr/ADR-005.md) | Authentication & authorization |
| [ADR-006](docs/adr/ADR-006.md) | Open core strategy |
| [ADR-007](docs/adr/ADR-007.md) | Content/presentation separation |
| [ADR-008](docs/adr/ADR-008.md) | Media storage strategy |
| [ADR-009](docs/adr/ADR-009.md) | System bootstrap & initial admin |
| [ADR-010](docs/adr/ADR-010.md) | Database schema & Flyway conventions |
| [ADR-011](docs/adr/ADR-011.md) | Slug generation & uniqueness |
| [ADR-012](docs/adr/ADR-012.md) | DTO & mapping strategy |
| [ADR-013](docs/adr/ADR-013.md) | Post status transitions & permissions |
| [ADR-014](docs/adr/ADR-014.md) | JWT details & refresh token persistence |
| [ADR-015](docs/adr/ADR-015.md) | Error & validation catalog |
| [ADR-016](docs/adr/ADR-016.md) | Configuration & Spring profiles |

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

The short version:
- Branch: `feature/*` or `fix/*`
- Commits: [Conventional Commits](https://www.conventionalcommits.org/)
- Core must not depend on plugins or external modules
- No breaking changes to public APIs without prior discussion

---

## License

The core of this project is open source. See [LICENSE](LICENSE) for details.
