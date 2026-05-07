# Database & Data Integrity Guide

This guide explains how the database works, how to keep your data safe, and how to handle schema changes as the application evolves.

---

## Table of Contents

- [Database Overview](#database-overview)
- [How the Schema is Managed (Flyway)](#how-the-schema-is-managed-flyway)
- [Schema Diagram](#schema-diagram)
- [Data Integrity Rules](#data-integrity-rules)
- [Backups](#backups)
- [Restoring from Backup](#restoring-from-backup)
- [Migrating to a Production Database (PostgreSQL)](#migrating-to-a-production-database-postgresql)
- [Writing New Migrations](#writing-new-migrations)

---

## Database Overview

The application uses **H2**, a lightweight Java-based relational database that stores data as files on disk. This means:

- ✅ **No database server to install** — it runs inside the application
- ✅ **Zero configuration** — it works out of the box
- ✅ **SQL-compatible** — it supports standard SQL, making migration to PostgreSQL/MySQL straightforward
- ⚠️ **Single-server only** — H2 is not designed for multi-server / high-availability deployments

**Database files are stored in:**
```
data/wishaw.mv.db     ← Main data file
data/wishaw.lock.db   ← Lock file (only exists while the app is running)
```

> ⚠️ These files **are your entire database**. If they are lost, all data is lost. Always back them up.

---

## How the Schema is Managed (Flyway)

The database schema is managed by [Flyway](https://flywaydb.org/), a migration tool that:

1. Looks in `src/main/resources/db/migration/` for SQL scripts
2. Runs any scripts that haven't been applied yet (tracked in a `flyway_schema_history` table)
3. Runs automatically every time the application starts

### Current Migrations

| File | Purpose |
|------|---------|
| `V1.0.0__initial_schema.sql` | Creates all tables: `centre`, `app_user`, `badge_category`, `level_definition`, `game`, `module`, `challenge`, `challenge_submission`, `metadata` |
| `V1.0.1__seed_badge_categories.sql` | Seeds the 5 badge categories, 4 level definitions (Bronze/Silver/Gold/Platinum), and 4 games (Minecraft, Rocket League, Fortnite, Generic) |
| `V1.0.2__seed_test_users.sql` | Creates a test centre ("Wishaw YMCA") and 3 test user accounts (for development only) |

### Key Rules

- **Never edit a migration that has already been applied.** Flyway tracks each migration by its checksum. Changing an applied file will cause startup failures.
- **Always add new changes as new migration files** with a higher version number (e.g., `V1.0.3__description.sql`).
- **Migrations are irreversible** — there is no automatic rollback. Always back up before deploying changes.

---

## Schema Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│   centre     │     │   metadata   │     │  badge_category  │
├──────────────┤     ├──────────────┤     ├──────────────────┤
│ id (PK)      │     │ id (PK)      │     │ id (PK)          │
│ name         │     │ icon         │     │ display_name     │
│ active       │     │ link         │     │ description      │
└──────┬───────┘     └──────┬───────┘     └────────┬─────────┘
       │                    │                      │
       │         ┌──────────┼──────────────────────┘
       │         │          │
┌──────┴─────────┴──┐      │         ┌──────────────────┐
│     app_user      │      │         │ level_definition  │
├─────────────���─────┤      │         ├───────────���──────┤
│ id (PK)           │      │         │ id (PK)          │
│ centre_id (FK)    │      │         │ name             │
│ metadata_id (FK)  │      │         │ min_xp           │
│ parent_id (FK)    │      │         │ max_xp           │
│ username          │      │         └──────────────────┘
│ password_hash     │      │
│ role              │      │         ┌──────────────┐
│ active            │      │         │    game       │
└──────┬────────────┘      │         ├──────────────┤
       │                   │         │ id (PK)      │
       │                   │         │ display_name │
       │                   │         │ active       │
       │                   │         └──────┬───────┘
       │                   │                │
       │         ┌─────────┴────────────────┴──┐
       │         │          module              │
       │         ├─────────────────────────────┤
       │         │ id (PK)                     │
       │         │ metadata_id (FK)            │
       │         │ display_name                │
       │         │ description                 │
       │         │ game_id (FK)                │
       │         │ active                      │
       │         └──────────────┬──────────────┘
       │                        │
       │         ┌──────────────┴──────────────┐
       │         │        challenge            │
       │         ├─────────────────────────────┤
       │         │ id (PK)                     │
       │         │ module_id (FK)              │
       │         │ metadata_id (FK)            │
       │         │ display_name                │
       │         │ description                 │
       │         │ badge_category_id (FK)      │
       │         │ xp_value                    │
       │         └──────────────┬──────────────┘
       │                        │
       │    ┌───────────────────┴──────────────┐
       │    │     challenge_submission         │
       │    ├──────────────────────────────────┤
       └���───│ submitted_by (FK -> app_user)    │
            │ reviewed_by (FK -> app_user)     │
            │ id (PK)                          │
            │ challenge_id (FK)                │
            │ note_text                        │
            │ status (SUBMITTED/APPROVED/      │
            │         REJECTED)                │
            │ submitted_ts                     │
            │ reviewed_ts                      │
            │ reviewer_comment                 │
            └──────────────────────────────────┘
```

---

## Data Integrity Rules

The following rules are enforced at the database level:

### Foreign Key Constraints
- Every **user** must belong to a **centre** (`centre_id NOT NULL`)
- Every **challenge** must belong to a **module** and a **badge category**
- Every **module** must belong to a **game**
- Every **submission** must reference a **challenge** and a **submitting user**
- A **parent user** reference is optional (for future parent-child linking)

### Unique Constraints
- `centre.name` is unique — no two centres can have the same name
- `app_user.username` is unique globally — no duplicate usernames across any centre

### Not-Null Constraints
- Required fields (e.g. `username`, `password_hash`, `role`, `display_name`, `xp_value`) cannot be empty

### XP Integrity
- XP is **calculated on-the-fly** from approved submissions — it is never stored as a separate value
- This means XP totals are always accurate and cannot become out of sync
- Deleting or rejecting a submission automatically recalculates the XP total
- The `XpService` class handles all XP calculation deterministically

### Centre Isolation
- All admin and coach queries filter by the current user's `centre_id`
- Users from one centre cannot see or modify data from another centre
- Only the global leaderboard crosses centre boundaries (read-only)

---

## Backups

### Manual Backup (Recommended Before Any Update)

1. **Stop the application** (to ensure no writes are in progress)
2. **Copy the database file:**

   **Windows:**
   ```cmd
   copy data\wishaw.mv.db data\wishaw.mv.db.backup
   ```

   **Linux / Mac:**
   ```bash
   cp data/wishaw.mv.db data/wishaw.mv.db.backup-$(date +%Y-%m-%d)
   ```

3. **Restart the application**

### Automated Backup (Linux Cron Job)

Create a script at `/opt/wishaw/backup.sh`:

```bash
#!/bin/bash
BACKUP_DIR="/opt/wishaw/backups"
DB_FILE="/opt/wishaw/data/wishaw.mv.db"
TIMESTAMP=$(date +%Y-%m-%d_%H%M%S)

mkdir -p "$BACKUP_DIR"
cp "$DB_FILE" "$BACKUP_DIR/wishaw.mv.db.$TIMESTAMP"

# Keep only the last 30 backups
ls -t "$BACKUP_DIR"/wishaw.mv.db.* | tail -n +31 | xargs rm -f 2>/dev/null
```

Schedule it with cron:
```bash
# Run daily at 2 AM
0 2 * * * /opt/wishaw/backup.sh
```

> 💡 **Tip:** For critical deployments, also copy backups to an offsite location (e.g., cloud storage bucket).

---

## Restoring from Backup

1. **Stop the application**
2. **Replace the database file** with the backup:
   ```bash
   cp data/wishaw.mv.db.backup-2026-03-31 data/wishaw.mv.db
   ```
3. **Start the application** — Flyway will detect that migrations are already applied and skip them

> ⚠️ Any data created after the backup was taken will be lost. This is why frequent backups are important.

---

## Migrating to a Production Database (PostgreSQL)

For larger deployments or multi-server setups, you may want to migrate from H2 to PostgreSQL. Here's how:

### Step 1: Add the PostgreSQL Driver

In `pom.xml`, replace:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>
```

With:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
```

### Step 2: Update Configuration

In `application.properties`:
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=wishaw
quarkus.datasource.password=your-secure-password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/wishaw
```

### Step 3: Adjust Migration SQL

Some H2-specific SQL may need updating for PostgreSQL:

| H2 Syntax | PostgreSQL Equivalent |
|------------|----------------------|
| `RANDOM_UUID()` | `gen_random_uuid()` (PostgreSQL 13+) |
| `CLOB` | `TEXT` |
| `BOOLEAN NOT NULL DEFAULT TRUE` | Same — works in both |

Create PostgreSQL-compatible versions of the migration scripts, or use Flyway's database-specific migration folders.

### Step 4: Export Existing Data

Use the H2 console or a tool like [DBeaver](https://dbeaver.io/) to export data from H2 and import into PostgreSQL.

---

## Writing New Migrations

When you need to change the database schema:

### 1. Create a New File

Add a new `.sql` file in `src/main/resources/db/migration/` with the next version number:

```
V1.0.3__add_avatar_to_user.sql
```

**Naming convention:** `V{major}.{minor}.{patch}__{description}.sql` (note the double underscore `__`)

### 2. Write the SQL

```sql
-- V1.0.3 - Add avatar URL column to app_user
ALTER TABLE app_user ADD COLUMN avatar_url VARCHAR(500);
```

### 3. Test Locally

Run the application in dev mode — Flyway will apply the migration automatically:
```cmd
mvnw.cmd quarkus:dev
```

### 4. Commit and Deploy

The migration will be applied automatically on the next deployment when the application starts.

### Best Practices

- ✅ Make migrations **additive** (add columns, tables) rather than destructive (drop columns)
- ✅ Use `ALTER TABLE ADD COLUMN` with a default value when adding non-nullable columns
- ✅ Test migrations against a copy of production data before deploying
- ❌ Never rename or delete a migration file that has been applied
- ❌ Never modify the content of an applied migration
