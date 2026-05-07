# API Reference

Complete reference for all REST API endpoints. All endpoints accept and return **JSON** (`application/json`).

For interactive documentation, start the app and visit: **http://localhost:8080/q/swagger-ui**

---

## Table of Contents

- [Authentication](#authentication)
- [Player APIs](#player-apis)
- [Admin APIs](#admin-apis)
- [Leaderboards](#leaderboards)
- [Management APIs](#management-apis)
  - [Centres](#centres)
  - [Users](#users)
  - [Games](#games)
  - [Modules](#modules)
  - [Challenges](#challenges)
  - [Badge Categories](#badge-categories)
  - [Level Definitions](#level-definitions)
  - [Metadata](#metadata)
  - [Submissions](#submissions)
- [Error Responses](#error-responses)

---

## Authentication

All endpoints (except login) require a valid session cookie. The cookie is set automatically on login.

### POST /auth/login

Log in and receive a session cookie.

**Access:** Public (no authentication required)

**Request:**
```json
{ "username": "string", "password": "string" }
```

**Response (200):**
```json
{
  "userId": "uuid",
  "username": "string",
  "role": "PLAYER",
  "centreId": "uuid",
  "displayName": "string"
}
```

The response includes a `Set-Cookie` header (`wishaw-session`) that must be sent with all subsequent requests. Most HTTP clients and browsers handle this automatically.

**Errors:**
- `400` — Username or password missing
- `401` — Invalid credentials or account disabled

---

### POST /auth/logout

Log out and clear the session cookie.

**Access:** Any authenticated user

**Response (200):**
```json
{ "message": "Logged out" }
```

---

### GET /auth/me

Get the currently authenticated user's details.

**Access:** Any authenticated user

**Response (200):**
```json
{
  "userId": "uuid",
  "username": "string",
  "role": "PLAYER",
  "centreId": "uuid",
  "displayName": "string"
}
```

---

## Player APIs

Endpoints for players to view their own progress and submit challenges.

### GET /me/profile

Get the current user's profile.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Response (200):**
```json
{
  "userId": "uuid",
  "displayName": "string",
  "avatarUrl": "string|null"
}
```

---

### GET /me/badges

Get the current user's badge progress across all categories.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Response (200):**
```json
{
  "badges": [
    {
      "badgeCategory": "Game Mastery",
      "xp": 45,
      "level": "Silver",
      "nextLevelAtXp": 71
    }
  ]
}
```

XP is calculated live from all approved submissions. The `level` field shows the current rank (Bronze/Silver/Gold/Platinum). `nextLevelAtXp` is the XP threshold for the next level, or `-1` if at the maximum level.

---

### GET /me/modules

Get all active modules with the current user's completion progress.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Response (200):**
```json
{
  "modules": [
    {
      "moduleId": "uuid",
      "name": "Minecraft: Building Basics",
      "game": "Minecraft",
      "progress": {
        "approved": 3,
        "total": 10
      }
    }
  ]
}
```

---

### POST /me/challenges/{challengeId}/submit

Submit evidence for a challenge. The submission will be reviewed by a coach or admin.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Request:**
```json
{ "noteText": "I built a shelter using cobblestone and wooden planks" }
```

**Response (200):**
```json
{
  "submissionId": "uuid",
  "status": "SUBMITTED"
}
```

**Errors:**
- `404` — Challenge not found

---

## Admin APIs

Endpoints for coaches and admins to manage their centre's users and submissions.

### GET /admin/centre/users

List all users in the admin's centre.

**Access:** COACH, ADMIN

**Response (200):**
```json
{
  "users": [
    {
      "userId": "uuid",
      "username": "string",
      "displayName": "string",
      "role": "PLAYER",
      "active": true
    }
  ]
}
```

---

### GET /admin/centre/submissions?status={status}

List submissions for the admin's centre, filtered by status.

**Access:** COACH, ADMIN

**Query Parameters:**
| Parameter | Default | Options |
|-----------|---------|---------|
| `status` | `SUBMITTED` | `SUBMITTED`, `APPROVED`, `REJECTED` |

**Response (200):**
```json
{
  "submissions": [
    {
      "submissionId": "uuid",
      "challengeId": "uuid",
      "challengeName": "Build a Shelter",
      "username": "player1",
      "displayName": "player1",
      "noteText": "I built a shelter",
      "submittedAt": "2026-03-31T10:30:00Z"
    }
  ]
}
```

---

### POST /admin/submissions/{submissionId}/approve

Approve a pending submission. XP is awarded when status becomes APPROVED.

**Access:** COACH, ADMIN

**Request:**
```json
{ "reviewerComment": "Great work!" }
```
The `reviewerComment` is optional for approvals.

**Response (200):**
```json
{
  "submissionId": "uuid",
  "status": "APPROVED"
}
```

**Errors:**
- `400` — Submission already reviewed
- `404` — Submission not found (or belongs to a different centre)

---

### POST /admin/submissions/{submissionId}/reject

Reject a pending submission. A reviewer comment is **required** for rejections.

**Access:** COACH, ADMIN

**Request:**
```json
{ "reviewerComment": "Please provide more detail about what you built" }
```

**Response (200):**
```json
{
  "submissionId": "uuid",
  "status": "REJECTED"
}
```

**Errors:**
- `400` — Reviewer comment is required; or submission already reviewed
- `404` — Submission not found (or belongs to a different centre)

---

## Leaderboards

### GET /leaderboards/centre

Ranked leaderboard of players in the current user's centre.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Response (200):**
```json
{
  "rows": [
    {
      "rank": 1,
      "username": "player1",
      "displayName": "player1",
      "totalXp": 150
    }
  ]
}
```

Only active PLAYER accounts are included. Sorted by total XP (descending).

---

### GET /leaderboards/global

Ranked leaderboard of all players across all centres.

**Access:** PLAYER, PARENT, COACH, ADMIN

**Response (200):**
```json
{
  "rows": [
    {
      "rank": 1,
      "centreName": "Wishaw YMCA",
      "username": "player1",
      "displayName": "player1",
      "totalXp": 150
    }
  ]
}
```

---

## Management APIs

CRUD endpoints for managing reference data. All require the **ADMIN** role.

### Centres

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/centres` | List all centres |
| `GET` | `/manage/centres/{id}` | Get a centre by ID |
| `POST` | `/manage/centres` | Create a new centre |
| `PUT` | `/manage/centres/{id}` | Update a centre |

**Create request:**
```json
{ "name": "Glasgow YMCA" }
```

**Update request:**
```json
{ "name": "New Name", "active": false }
```

---

### Users

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/users` | List users in admin's centre |
| `GET` | `/manage/users/{id}` | Get a user by ID (same centre only) |
| `POST` | `/manage/users` | Create a new user |
| `PUT` | `/manage/users/{id}` | Update a user |

**Create request:**
```json
{
  "username": "new_player",
  "password": "securepassword",
  "role": "PLAYER",
  "centreId": "uuid (optional, defaults to admin's centre)",
  "metadataId": "uuid (optional)",
  "parentId": "uuid (optional)"
}
```

**Update request:**
```json
{
  "role": "COACH",
  "active": true,
  "password": "new-password"
}
```

---

### Games

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/games` | List all games |
| `GET` | `/manage/games/{id}` | Get a game by ID |
| `POST` | `/manage/games` | Create a new game |
| `PUT` | `/manage/games/{id}` | Update a game |

**Create request:**
```json
{ "displayName": "Valorant", "active": true }
```

---

### Modules

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/modules` | List all modules |
| `GET` | `/manage/modules/{id}` | Get a module by ID |
| `POST` | `/manage/modules` | Create a new module |
| `PUT` | `/manage/modules/{id}` | Update a module |

**Create request:**
```json
{
  "displayName": "Minecraft: Building Basics",
  "description": "Learn the fundamentals of building in Minecraft",
  "gameId": "uuid",
  "active": true,
  "metadataId": "uuid (optional)"
}
```

---

### Challenges

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/challenges` | List all challenges |
| `GET` | `/manage/challenges/{id}` | Get a challenge by ID |
| `POST` | `/manage/challenges` | Create a new challenge |
| `PUT` | `/manage/challenges/{id}` | Update a challenge |

**Create request:**
```json
{
  "displayName": "Build a Shelter",
  "description": "Build a basic shelter that protects from mobs",
  "moduleId": "uuid",
  "badgeCategoryId": "uuid",
  "xpValue": 10,
  "metadataId": "uuid (optional)"
}
```

---

### Badge Categories

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/badge-categories` | List all badge categories |
| `GET` | `/manage/badge-categories/{id}` | Get a badge category by ID |
| `POST` | `/manage/badge-categories` | Create a new badge category |
| `PUT` | `/manage/badge-categories/{id}` | Update a badge category |

**Create request:**
```json
{
  "displayName": "Leadership",
  "description": "Demonstrating leadership qualities"
}
```

---

### Level Definitions

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/level-definitions` | List all level definitions |
| `GET` | `/manage/level-definitions/{id}` | Get a level definition by ID |
| `POST` | `/manage/level-definitions` | Create a new level definition |
| `PUT` | `/manage/level-definitions/{id}` | Update a level definition |

**Create request:**
```json
{
  "name": "Diamond",
  "minXp": 200,
  "maxXp": 500
}
```

---

### Metadata

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/metadata` | List all metadata |
| `GET` | `/manage/metadata/{id}` | Get metadata by ID |
| `POST` | `/manage/metadata` | Create new metadata |
| `PUT` | `/manage/metadata/{id}` | Update metadata |

Metadata objects can be linked to users, modules, or challenges to provide icons and links.

**Create request:**
```json
{
  "icon": "https://example.com/icon.png",
  "link": "https://example.com/resource"
}
```

---

### Submissions

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/manage/submissions` | List all submissions (admin's centre) |
| `GET` | `/manage/submissions/{id}` | Get a submission by ID |

---

## Error Responses

All error responses follow a consistent format:

```json
{ "error": "Description of what went wrong" }
```

### HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `201` | Created (for POST requests that create resources) |
| `400` | Bad Request — missing or invalid input |
| `401` | Unauthorised — not logged in or invalid session |
| `403` | Forbidden — logged in but insufficient permissions |
| `404` | Not Found — resource doesn't exist (or belongs to a different centre) |
| `500` | Internal Server Error — unexpected failure (check logs) |

