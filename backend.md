# Backend Design
## Wishaw YMCA Esports - Login App
### Quarkus + Quarkus Security

## 1. Purpose
This document defines the backend architecture and API design for the Wishaw YMCA Esports Login App.

Backend responsibilities:
- Authentication and authorisation
- Enforcing centre isolation
- Managing badges, XP, modules, challenges
- Admin approvals
- Leaderboards

## 2. Technology Stack
- Quarkus
- Quarkus Security
- REST APIs (JSON over HTTPS)
- Hibernate ORM / JPA
- Relational database (H2 file system)

## 3. Security Model

### 3.1 Authentication
- Username + password
- Passwords stored as secure hashes (bcrypt or equivalent)
- No email addresses
- No self-service password reset
- Admin-initiated reset only

### 3.2 Roles
- PLAYER
- PARENT
- COACH
- ADMIN

### 3.3 Centre isolation
- Every user belongs to exactly one centre
- Every query involving user data must filter by centre_id
- SYSTEM_ADMIN may cross centres
- CENTRE_ADMIN restricted to their own centre

## 4. Domain Model

### 4.1 Centre
- id (UUID)
- name
- active

### 4.2 User
- id (UUID)
- centre_id (UUID)
- metadata_id (UUID) (optional)
- parent_id (optional)
- username
- password_hash
- role (PLAYER, PARENT, COACH, ADMIN)
- active

### 4.3 BadgeCategory (Configurable)

Existing Categories:
GAME MASTERY, TEAMWORK, ESPORTS CITIZEN, PERSONAL DEVELOPMENT, DIGITAL SKILLS

Fields:
- id (UUID)
- display_name
- description

### 4.4 LevelDefinition (Configurable)
Defines XP to level mapping per badge category.

Fields:
- id (UUID)
- name (Bronze, Silver, Gold, Platinum, etc.)
- min_xp
- max_xp

### 4.5 Module
Represents an esports module.

Fields:
- id (UUID)
- metadata_id (UUID)
- display_name
- description
- game_id (UUID)
- active

### Game

Fields:
- id (UUID)
- display_name (MINECRAFT, ROCKET LEAGUE, FORTNITE, GENERIC))
- active


### 4.6 Challenge
A single task within a module.

Fields:
- id (UUID)
- module_id (UUID)
- metadata_id (UUID)
- display_name
- description
- badge_category_id
- xp_value


### Metadata

Fields:
- id(UUID)
- icon (optional)
- link (optional)

### 4.7 ChallengeSubmission
Text-only evidence.

Fields:
- id
- challenge_id
- note_text
- status (SUBMITTED, APPROVED, REJECTED)
- submitted_ts
- submitted_by (user_id)
- reviewed_ts
- reviewed_by (user_id)
- reviewer_comment

XP is awarded only when status becomes APPROVED.

## 5. XP Handling
XP may be implemented as:
- Derived value calculated from approved submissions, or
- Persisted ledger table

Constraints:
- XP totals must be centre-safe
- XP recalculation must be deterministic

## 6. REST API Surface
All endpoints are JSON over HTTPS.

### 6.1 Authentication
#### POST /auth/login
Request:
```json
{ "username": "string", "password": "string" }
```
Response (success):
```json
{ "userId": "uuid", "username": "string", "role": "PLAYER", "centreId": "uuid", "displayName": "string" }
```

#### POST /auth/logout
No body.

#### GET /auth/me
Response:
```json
{ "userId": "uuid", "username": "string", "role": "PLAYER", "centreId": "uuid", "displayName": "string" }
```

### 6.2 Player APIs
#### GET /me/profile
Response:
```json
{ "userId": "uuid", "displayName": "string", "avatarUrl": "string|null" }
```

#### GET /me/badges
Response:
```json
{ "badges": [ { "badgeCategory": "GAME_MASTERY", "xp": 0, "level": "Bronze", "nextLevelAtXp": 31 } ] }
```

#### GET /me/modules
Response:
```json
{ "modules": [ { "moduleId": "uuid", "name": "string", "game": "MINECRAFT", "progress": { "approved": 0, "total": 0 } } ] }
```

#### POST /me/challenges/{challengeId}/submit
Request:
```json
{ "noteText": "string" }
```
Response:
```json
{ "submissionId": "uuid", "status": "SUBMITTED" }
```

### 6.3 Admin APIs
#### GET /admin/centre/users
Response:
```json
{ "users": [ { "userId": "uuid", "username": "string", "displayName": "string", "role": "PLAYER", "active": true } ] }
```

#### GET /admin/centre/submissions?status=SUBMITTED
Response:
```json
{ "submissions": [ { "submissionId": "uuid", "challengeId": "uuid", "challengeName": "string", "username": "string", "displayName": "string", "noteText": "string", "submittedAt": "iso-8601" } ] }
```

#### POST /admin/submissions/{submissionId}/approve
Request:
```json
{ "reviewerComment": "string|null" }
```
Response:
```json
{ "submissionId": "uuid", "status": "APPROVED" }
```

#### POST /admin/submissions/{submissionId}/reject
Request:
```json
{ "reviewerComment": "string" }
```
Response:
```json
{ "submissionId": "uuid", "status": "REJECTED" }
```

### 6.4 Leaderboards
#### GET /leaderboards/centre
Response:
```json
{ "rows": [ { "rank": 1, "username": "string", "displayName": "string", "totalXp": 0 } ] }
```

#### GET /leaderboards/global
Response:
```json
{ "rows": [ { "rank": 1, "centreName": "string", "username": "string", "displayName": "string", "totalXp": 0 } ] }
```

## 7. Backend Constraints (Hard Rules)
- No registration endpoints
- No file upload endpoints
- No email fields
- No cross-centre data leakage
- All approval actions must be auditable

## 8. Intended GitHub Copilot Usage
This document should guide Copilot to:
- Generate JPA entities
- Create REST resources
- Apply role-based security annotations
- Enforce centre filtering in queries

Copilot must not introduce:
- New roles
- Registration flows
- File handling
- Parent or guardian logic
