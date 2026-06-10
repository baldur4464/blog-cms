# Contributing to the Blog CMS

First of all, thank you for your interest in contributing to this project.

This project aims to build a modern, extensible, open-source blog CMS using Java (Spring Boot), React, and PostgreSQL. The goal is to keep the core simple, stable, and highly extensible through clean architecture and well-defined extension points.

---

## Project Principles

Before contributing, please understand the core principles of this project:

- **Core is minimal** – only essential CMS functionality lives in the core
- **Extensibility first** – features should be designed with future plugins and extensions in mind
- **Clear boundaries** – core must not depend on plugins or external modules
- **API-first design** – all functionality is exposed via stable REST APIs
- **Markdown-based content** – posts are stored in Markdown format
- **UUID-based IDs** – all entities use UUIDs
- **Auditing enabled** – all core entities track creation and update timestamps

---

## Architecture Overview

The project follows a **modular monolith** approach:

```
backend/
├── auth
├── user
├── post
├── category
├── tag
├── media
├── extension
├── common
└── config
```

Each module contains its own:

- controller
- service
- repository
- domain models

> This is a **package-by-feature** architecture, not a layered architecture.

---

## Branching Strategy

We use a simple Git workflow:

| Branch | Purpose |
|--------|---------|
| `main` | Stable, production-ready code |
| `feature/*` | New features |
| `fix/*` | Bug fixes |

**Examples:**

```
feature/post-editor
feature/auth-jwt
fix/slug-generation
```

---

## Commit Convention

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add post creation endpoint
fix: resolve slug uniqueness issue
refactor: simplify post service logic
docs: update API documentation
chore: update dependencies
```

---

## Getting Started

### Backend

**Requirements:**
- Java 21+
- Maven or Gradle
- PostgreSQL

**Run locally:**

```bash
./mvnw spring-boot:run
```

### Frontend

**Requirements:**
- Node.js 18+

**Run locally:**

```bash
npm install
npm run dev
```

---

## Testing Guidelines

- Write unit tests for service layer logic
- Use integration tests for API endpoints
- Avoid testing framework internals
- Prefer meaningful tests over high coverage

---

## Code Style Guidelines

### Java (Backend)

- Use clear, domain-driven naming
- Keep services focused and small
- Avoid business logic in controllers
- Prefer constructor injection

**Example:**

```java
@RestController
@RequiredArgsConstructor
public class PostController {
}
```

### React (Frontend)

- Use functional components only
- Prefer hooks over class components
- Keep components small and reusable
- Use TypeScript strictly

---

## Extension Philosophy

This project is designed to be **extended, not modified**.

- Core functionality should remain stable
- New features should ideally be implemented via:
  - Events
  - Service interfaces
  - Extension points (future plugin system)

> Do not modify core logic if it can be extended cleanly.

---

## What NOT to Do

- Do not introduce breaking changes to public APIs without discussion
- Do not couple features across modules
- Do not add UI-specific logic into backend core
- Do not bypass the service layer
- Do not introduce tight coupling between modules

---

## Reporting Issues

When opening an issue, please include:

- Steps to reproduce
- Expected behavior
- Actual behavior
- Environment details (if relevant)

---

## Pull Request Process

1. Create a feature branch
2. Implement changes
3. Add tests if applicable
4. Ensure code compiles and passes tests
5. Submit a pull request
6. Describe **what** and **why**, not just how

---

## Philosophy

This project is not just a CMS.

It is an attempt to build a clean, extensible, developer-friendly content platform that can evolve into a full ecosystem over time.

Keep contributions:

- clean
- minimal
- extensible
- consistent with architecture

---

*Thank you for contributing.*
