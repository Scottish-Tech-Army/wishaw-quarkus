# Configuration Reference

All application settings are defined in `src/main/resources/application.properties`. Every property can be **overridden at runtime** using environment variables — no code changes required.

---

## Table of Contents

- [How to Override Settings](#how-to-override-settings)
- [Database](#database)
- [Hibernate ORM](#hibernate-orm)
- [Flyway (Database Migrations)](#flyway-database-migrations)
- [HTTP Server](#http-server)
- [Security](#security)
- [OpenAPI / Swagger UI](#openapi--swagger-ui)
- [Test Profile](#test-profile)
- [Profile-Specific Configuration](#profile-specific-configuration)
- [Example: Production Environment Variables](#example-production-environment-variables)

---

## How to Override Settings

Quarkus converts property names to environment variables automatically. The rule is:

1. Replace `.` with `_`
2. Replace `-` with `_`
3. Convert to UPPERCASE

**Example:**
```
quarkus.datasource.jdbc.url  →  QUARKUS_DATASOURCE_JDBC_URL
```

You can set these as system environment variables, pass them on the command line, or include them in a Docker `environment` block.

**Command line:**
```bash
java -Dquarkus.http.port=9090 -jar quarkus-app/quarkus-run.jar
```

**Environment variable:**
```bash
export QUARKUS_HTTP_PORT=9090
```

---

## Database

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.datasource.db-kind` | `h2` | Database type. Change if migrating to PostgreSQL, MySQL, etc. |
| `quarkus.datasource.username` | `wishaw` | Database username |
| `quarkus.datasource.password` | `wishaw` | Database password |
| `quarkus.datasource.jdbc.url` | `jdbc:h2:file:./data/wishaw;AUTO_SERVER=TRUE` | JDBC connection URL. The `AUTO_SERVER=TRUE` flag allows multiple processes to connect. |

## Hibernate ORM

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.hibernate-orm.database.generation` | `none` | Schema management strategy. Set to `none` because Flyway handles migrations. **Do not change.** |
| `quarkus.hibernate-orm.log.sql` | `true` | Logs every SQL query. Set to `false` in production for performance. |

## Flyway (Database Migrations)

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.flyway.migrate-at-start` | `true` | Automatically applies pending migrations when the app starts. |
| `quarkus.flyway.locations` | `db/migration` | Folder containing SQL migration scripts. |

## HTTP Server

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.http.port` | `8080` | Port the API listens on. Override with `QUARKUS_HTTP_PORT`. |

## Security

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.http.auth.session.encryption-key` | `wishaw-ymca-esports-secret-key-32chars!` | Encryption key for session data. **Must be changed in production.** Must be at least 32 characters. |
| `quarkus.http.auth.basic` | `false` | HTTP Basic auth. Disabled — the app uses custom cookie authentication. |
| `quarkus.http.auth.form.enabled` | `false` | Form-based auth. Disabled — the app uses custom cookie authentication. |
| `quarkus.http.auth.proactive` | `false` | Whether to authenticate every request proactively. Disabled to allow public endpoints (e.g. login). |

## OpenAPI / Swagger UI

| Property | Default | Description |
|----------|---------|-------------|
| `quarkus.smallrye-openapi.info-title` | `Wishaw YMCA Esports API` | Title shown in Swagger UI |
| `quarkus.smallrye-openapi.info-version` | `1.0.0` | API version shown in Swagger UI |
| `quarkus.swagger-ui.always-include` | `true` | Whether Swagger UI is available. Set to `false` in production. |
| `quarkus.smallrye-openapi.store-schema-directory` | `target/openapi` | Where the generated OpenAPI spec files are saved during build. |

## Test Profile

Properties prefixed with `%test.` are only active when running tests:

| Property | Value | Description |
|----------|-------|-------------|
| `%test.quarkus.datasource.jdbc.url` | `jdbc:h2:mem:wishaw_test;DB_CLOSE_DELAY=-1` | Uses an in-memory database for tests (no files created). |
| `%test.quarkus.hibernate-orm.database.generation` | `none` | Schema managed by Flyway even in tests. |
| `%test.quarkus.flyway.migrate-at-start` | `true` | Migrations run automatically during tests. |

---

## Profile-Specific Configuration

Quarkus supports [configuration profiles](https://quarkus.io/guides/config#profiles). Common profiles:

| Profile | When Active | How to Activate |
|---------|-------------|-----------------|
| `dev` | Running `mvnw quarkus:dev` | Automatic |
| `test` | Running tests | Automatic |
| `prod` | Default when running the built JAR | Automatic |

You can add production-specific settings by prefixing with `%prod.`:

```properties
%prod.quarkus.hibernate-orm.log.sql=false
%prod.quarkus.swagger-ui.always-include=false
%prod.quarkus.http.port=8080
```

---

## Example: Production Environment Variables

```bash
# Database (keep on persistent storage)
QUARKUS_DATASOURCE_JDBC_URL=jdbc:h2:file:/opt/wishaw/data/wishaw;AUTO_SERVER=TRUE

# Security (change this!)
QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY=your-random-secret-key-at-least-32-chars

# Disable dev tools
QUARKUS_SWAGGER_UI_ALWAYS_INCLUDE=false
QUARKUS_HIBERNATE_ORM_LOG_SQL=false

# Port
QUARKUS_HTTP_PORT=8080
```
