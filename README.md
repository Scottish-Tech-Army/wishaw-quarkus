# Wishaw YMCA — eSports Badge Portal (Backend API)

> A backend API for the Wishaw YMCA **Pathways** programme, replacing manual spreadsheets with an automated badge, XP, and challenge-tracking system for young esports participants.

---

## Table of Contents

- [What This Project Does](#what-this-project-does)
- [Technology Overview](#technology-overview)
- [Quick Start (Development)](#quick-start-development)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Default Test Accounts](#default-test-accounts)
- [API Documentation](#api-documentation)
- [Licence](#licence)

---

## What This Project Does

This backend powers the eSports Badge Portal — a system that:

- **Tracks youth progress** through esports modules and challenges
- **Awards XP and badges** across five categories (Game Mastery, Teamwork, Esports Citizen, Personal Development, Digital Skills)
- **Automates levelling** (Bronze → Silver → Gold → Platinum) based on earned XP
- **Supports multiple centres** (e.g. different YMCA locations) with full data isolation
- **Provides admin tools** for coaches and admins to manage users, approve challenge submissions, and view leaderboards
- **Exposes a REST API** that any frontend (web app, PWA, mobile) can connect to

---

## Technology Overview

| Component        | Technology                                                     |
|------------------|----------------------------------------------------------------|
| Language         | Java 17                                                        |
| Framework        | [Quarkus 3.32](https://quarkus.io/) (supersonic Java framework)|
| Database         | H2 (file-based, zero-config relational database)               |
| ORM              | Hibernate ORM with Panache                                     |
| Migrations       | Flyway (versioned SQL scripts)                                 |
| Security         | Quarkus Security + custom HMAC-signed cookie auth              |
| Password Hashing | bcrypt                                                         |
| API Docs         | OpenAPI 3.0 / Swagger UI                                       |
| Build Tool       | Maven (with included wrapper `mvnw` / `mvnw.cmd`)             |
| Containerisation | Docker (Dockerfiles provided)                                  |

---

## Quick Start (Development)

### Prerequisites

- **Java 17+** — [Download from Adoptium](https://adoptium.net/)
- **No database installation needed** — H2 runs embedded

### Run the Application

**Windows:**
```cmd
mvnw.cmd quarkus:dev
```

**Mac / Linux:**
```bash
./mvnw quarkus:dev
```

The API will start at **http://localhost:8080**.

- **Swagger UI** (interactive API docs): http://localhost:8080/q/swagger-ui
- **Quarkus Dev UI**: http://localhost:8080/q/dev/
- **OpenAPI spec** (JSON): http://localhost:8080/q/openapi

The database is created automatically in the `data/` folder on first run. Flyway migrations seed it with reference data and test accounts.

---

## Project Structure

```
├── src/main/java/org/scottishtecharmy/wishaw/quarkus/
│   ├── model/            # JPA entities (database tables)
│   ├── repository/       # Data access layer (queries)
│   ├── resource/          # REST API endpoints
│   ├── dto/               # Request/response data objects
│   ├── service/           # Business logic (e.g. XP calculation)
│   └── security/          # Authentication & session handling
├── src/main/resources/
│   ├── application.properties   # App configuration
│   └── db/migration/            # Flyway SQL migration scripts
├── src/main/docker/             # Docker build files
├── data/                        # H2 database files (auto-created)
├── bruno/                       # Bruno API collection (for testing)
├── docs/                        # Project documentation
└── pom.xml                      # Maven build configuration
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Deployment Guide](docs/deployment-guide.md) | How to build, deploy, and run in production (bare metal, Docker, cloud) |
| [Configuration Reference](docs/configuration-reference.md) | All configurable settings (database, security, ports, etc.) |
| [Database & Data Integrity Guide](docs/database-and-data-integrity.md) | Schema overview, migrations, backups, and data safety |
| [Extending & Customising the App](docs/extending-and-customising.md) | How to add new badges, games, modules, migrate databases, and adapt for your organisation |
| [Support & Troubleshooting](docs/support-and-troubleshooting.md) | Common issues, logs, health checks, and maintenance tasks |
| [API Reference](docs/api-reference.md) | Complete REST API endpoint reference |
| [Security Model](docs/security-model.md) | Authentication, roles, centre isolation, and secrets management |
| [Backend Design](backend.md) | Original technical design document |

---

## Default Test Accounts

These accounts are created by the seed migration (`V1.0.2`) for **development only**:

| Username  | Password      | Role   | Centre      |
|-----------|---------------|--------|-------------|
| `admin1`  | `password123` | ADMIN  | Wishaw YMCA |
| `coach1`  | `password123` | STAFF  | Wishaw YMCA |
| `player1` | `password123` | PLAYER | Wishaw YMCA |

> ⚠️ **Change all passwords before deploying to production.** See the [Deployment Guide](docs/deployment-guide.md#production-checklist).

---

## API Documentation

Once the application is running, interactive API documentation is available at:

- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **OpenAPI JSON**: http://localhost:8080/q/openapi

You can also find a static copy of the OpenAPI spec at `target/openapi/openapi.yaml` after building.

For a human-readable API reference, see [docs/api-reference.md](docs/api-reference.md).

---

## Licence

See [LICENSE](LICENSE) for details.
