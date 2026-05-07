# Extending & Customising the App

This guide explains how to adapt the application for your organisation — adding new badges, games, centres, modules, and challenges — without needing to modify any code.

---

## Table of Contents

- [Overview: What Can Be Customised](#overview-what-can-be-customised)
- [Adding a New Centre](#adding-a-new-centre)
- [Adding New Users](#adding-new-users)
- [Adding New Games](#adding-new-games)
- [Adding New Badge Categories](#adding-new-badge-categories)
- [Changing Level Thresholds (XP Ranges)](#changing-level-thresholds-xp-ranges)
- [Adding New Modules and Challenges](#adding-new-modules-and-challenges)
- [Rebranding the Application](#rebranding-the-application)
- [Connecting a Frontend](#connecting-a-frontend)
- [Adding New API Features (For Developers)](#adding-new-api-features-for-developers)
- [Multi-Centre Deployment](#multi-centre-deployment)

---

## Overview: What Can Be Customised

| What | How | Code Changes? |
|------|-----|---------------|
| Centres (YMCA locations) | Admin API | No |
| Users (players, coaches, admins) | Admin API | No |
| Games (Minecraft, Fortnite, etc.) | Management API | No |
| Badge categories | Management API | No |
| Level thresholds (Bronze, Silver, etc.) | Management API | No |
| Modules and challenges | Management API | No |
| XP values per challenge | Management API | No |
| Database type (H2 → PostgreSQL) | Configuration | Minimal |
| New API endpoints | Code | Yes |
| New user roles | Code | Yes |

---

## Adding a New Centre

Centres represent physical locations (e.g. different YMCA branches). Each centre's data is fully isolated.

**API call** (requires ADMIN role):
```
POST /manage/centres
Content-Type: application/json

{
  "name": "Glasgow YMCA"
}
```

Or via a database migration for bulk setup:
```sql
-- V1.0.4__add_glasgow_centre.sql
INSERT INTO centre (id, name, active) VALUES
    ('00000000-0000-0000-0000-000000000002', 'Glasgow YMCA', TRUE);
```

---

## Adding New Users

Users are created by admins — there is no self-registration (by design, for safeguarding).

**API call** (requires ADMIN role):
```
POST /manage/users
Content-Type: application/json

{
  "username": "player_jane",
  "password": "securepassword",
  "role": "PLAYER"
}
```

Available roles:

| Role | What They Can Do |
|------|-----------------|
| `PLAYER` | View own profile, badges, modules; submit challenges |
| `PARENT` | View own profile and badges (future: view child's progress) |
| `COACH` | Everything a PLAYER can do, plus approve/reject submissions and view centre users |
| `ADMIN` | Everything, plus manage centres, users, games, modules, challenges, and badge categories |

Users are automatically assigned to the same centre as the admin who creates them, unless a `centreId` is explicitly provided.

---

## Adding New Games

Games group modules together (e.g. all Minecraft-related modules belong to the "Minecraft" game).

**API call** (requires ADMIN role):
```
POST /manage/games
Content-Type: application/json

{
  "displayName": "Valorant",
  "active": true
}
```

The seeded games are: **Minecraft**, **Rocket League**, **Fortnite**, **Generic**.

---

## Adding New Badge Categories

Badge categories are the five pillars of the Pathways programme. You can add more if needed.

**API call** (requires ADMIN role):
```
POST /manage/badge-categories
Content-Type: application/json

{
  "displayName": "Leadership",
  "description": "Demonstrating leadership qualities in team settings"
}
```

The seeded categories are: **Game Mastery**, **Teamwork**, **Esports Citizen**, **Personal Development**, **Digital Skills**.

---

## Changing Level Thresholds (XP Ranges)

Level definitions control how much XP is needed for each rank. These are global (not per-category).

**View current levels:**
```
GET /manage/level-definitions
```

**Create a new level:**
```
POST /manage/level-definitions
Content-Type: application/json

{
  "name": "Diamond",
  "minXp": 200,
  "maxXp": 500
}
```

**Update an existing level:**
```
PUT /manage/level-definitions/{id}
Content-Type: application/json

{
  "minXp": 121,
  "maxXp": 250
}
```

Default levels:

| Level | XP Range |
|-------|----------|
| Bronze | 0 – 30 |
| Silver | 31 – 70 |
| Gold | 71 – 120 |
| Platinum | 121+ |

> 💡 **Tip:** Ensure XP ranges don't overlap and there are no gaps, otherwise players may end up "Unranked".

---

## Adding New Modules and Challenges

Modules are groups of challenges within a game. Challenges are individual tasks that award XP.

### Step 1: Create a Module

```
POST /manage/modules
Content-Type: application/json

{
  "displayName": "Minecraft: Building Basics",
  "description": "Learn the fundamentals of building in Minecraft",
  "gameId": "<game-uuid>",
  "active": true
}
```

### Step 2: Create Challenges Within the Module

```
POST /manage/challenges
Content-Type: application/json

{
  "displayName": "Build a Shelter",
  "description": "Build a basic shelter that protects from mobs",
  "moduleId": "<module-uuid>",
  "badgeCategoryId": "<badge-category-uuid>",
  "xpValue": 10
}
```

Each challenge belongs to one **badge category** and awards a fixed amount of **XP** when approved. This means a single module can contain challenges spanning multiple badge categories.

---

## Rebranding the Application

To use this for a different organisation:

### Change the API Title

In `src/main/resources/application.properties`:
```properties
quarkus.smallrye-openapi.info-title=Your Organisation Esports API
quarkus.smallrye-openapi.info-description=Backend API for Your Organisation
```

### Change Seed Data

Create a new migration to add your organisation's centre and initial admin:
```sql
-- V1.0.3__your_organisation_setup.sql
INSERT INTO centre (id, name, active) VALUES
    (RANDOM_UUID(), 'Your Organisation Name', TRUE);
```

### Change the Session Cookie Name

In `SecurityConstants.java`, change:
```java
public static final String SESSION_COOKIE_NAME = "your-org-session";
```

---

## Connecting a Frontend

This backend is designed to work with **any frontend** — web app, PWA, mobile app, or even a command-line tool. The API is fully documented via OpenAPI.

### Key Integration Points

1. **Authentication**: `POST /auth/login` returns a `Set-Cookie` header. The frontend should include cookies in subsequent requests.
2. **Player data**: `GET /me/profile`, `GET /me/badges`, `GET /me/modules`
3. **Challenge submission**: `POST /me/challenges/{id}/submit`
4. **Admin dashboard**: `GET /admin/centre/users`, `GET /admin/centre/submissions`
5. **Leaderboards**: `GET /leaderboards/centre`, `GET /leaderboards/global`

### CORS Configuration

If your frontend runs on a different domain/port, you'll need to enable CORS. Add to `application.properties`:

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=https://your-frontend-domain.com
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type,Authorization
quarkus.http.cors.exposed-headers=Set-Cookie
quarkus.http.cors.access-control-allow-credentials=true
```

### Generating a Client SDK

The OpenAPI spec at `/q/openapi` can be used with tools like [OpenAPI Generator](https://openapi-generator.tech/) to auto-generate client libraries in JavaScript, TypeScript, Python, etc.:

```bash
npx openapi-generator-cli generate -i http://localhost:8080/q/openapi -g typescript-fetch -o ./client
```

---

## Adding New API Features (For Developers)

The codebase follows a consistent pattern. To add a new feature:

### 1. Create a JPA Entity

Add a new class in `src/main/java/.../model/`:
```java
@Entity
@Table(name = "your_table")
public class YourEntity extends PanacheEntityBase {
    @Id @GeneratedValue public UUID id;
    // fields...
}
```

### 2. Create a Repository

Add a class in `src/main/java/.../repository/`:
```java
@ApplicationScoped
public class YourRepository implements PanacheRepositoryBase<YourEntity, UUID> {
    // custom queries...
}
```

### 3. Create DTOs

Add request/response classes in `src/main/java/.../dto/`.

### 4. Create a Resource (REST Endpoint)

Add a class in `src/main/java/.../resource/`:
```java
@Path("/your-endpoint")
@RolesAllowed("ADMIN")
public class YourResource {
    // GET, POST, PUT methods...
}
```

### 5. Write a Migration

Add the table creation SQL in `src/main/resources/db/migration/V1.0.X__description.sql`.

---

## Multi-Centre Deployment

The application is designed to support multiple centres from day one.

### Single Deployment, Multiple Centres

The simplest approach — one running instance serves all centres:

1. Create a centre for each YMCA via the admin API
2. Create an ADMIN user for each centre
3. Each centre admin only sees their own users, submissions, and leaderboard
4. The global leaderboard shows cross-centre rankings

### Separate Deployments per Organisation

For full isolation (separate databases, separate servers):

1. Deploy a separate instance per organisation
2. Each gets its own database, config, and domain
3. No data sharing between instances

### Data Export

Currently, data can be exported via the H2 console or database tools. Future enhancements could include CSV export endpoints for reporting.
