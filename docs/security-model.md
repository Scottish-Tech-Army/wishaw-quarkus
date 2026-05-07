# Security Model

This document explains how authentication, authorisation, and data isolation work in the Wishaw YMCA eSports Badge Portal.

---

## Table of Contents

- [Overview](#overview)
- [Authentication Flow](#authentication-flow)
- [Session Cookie Details](#session-cookie-details)
- [Roles and Permissions](#roles-and-permissions)
- [Centre Isolation](#centre-isolation)
- [Password Security](#password-security)
- [Changing the Session Secret](#changing-the-session-secret)
- [Security Best Practices for Production](#security-best-practices-for-production)
- [Known Limitations and Future Improvements](#known-limitations-and-future-improvements)

---

## Overview

The application uses a **cookie-based authentication** system:

1. The user sends their username and password to `POST /auth/login`
2. The server verifies the credentials and returns an **HMAC-signed cookie**
3. The browser (or API client) sends that cookie with every subsequent request
4. The server verifies the cookie signature and looks up the user on each request

There are **no JWTs, no OAuth, and no third-party auth providers** — the system is intentionally simple and self-contained.

---

## Authentication Flow

```
┌──────────┐                          ┌──────────────┐
│  Client  │                          │   Backend    │
│(browser) │                          │   (Quarkus)  │
└────┬─────┘                          └──────┬───────┘
     │                                       │
     │  POST /auth/login                     │
     │  { username, password }               │
     │──────────────────────────────────────►│
     │                                       │
     │                          Verify password (bcrypt)
     │                          Create HMAC-signed cookie
     │                                       │
     │  200 OK                               │
     │  Set-Cookie: wishaw-session=...       │
     │◄──────────────────────────────────────│
     │                                       │
     │  GET /me/badges                       │
     │  Cookie: wishaw-session=...           │
     │──────────────────────────────────────►│
     │                                       │
     │                          Verify cookie HMAC
     │                          Look up user by username
     │                          Check role permissions
     │                                       │
     │  200 OK { badges: [...] }             │
     │◄──────────────────────────────────────│
```

---

## Session Cookie Details

The session cookie (`wishaw-session`) contains the username signed with HMAC-SHA256.

**Cookie format on the wire:**
```
base64url(username) . base64url(hmac-sha256-signature)
```

**Cookie properties:**
| Property | Value | Purpose |
|----------|-------|---------|
| `HttpOnly` | Yes | Prevents JavaScript from reading the cookie (XSS protection) |
| `SameSite` | Strict | Prevents the cookie being sent on cross-site requests (CSRF protection) |
| `Path` | `/` | Cookie is sent for all API paths |
| `Max-Age` | 86400 (24 hours) | Cookie expires after 24 hours |
| `Secure` | Not set (development) | **Must be added for production** to ensure cookie is only sent over HTTPS |

**Implementation files:**
- `security/SessionCookieUtil.java` — Encodes/decodes and signs/verifies the cookie value
- `security/CookieAuthMechanism.java` — Quarkus HTTP auth mechanism that reads the cookie on each request
- `security/TrustedIdentityProvider.java` — Resolves the username from the cookie to a full user identity
- `security/SecurityConstants.java` — Cookie name constant

---

## Roles and Permissions

### Available Roles

| Role | Description |
|------|-------------|
| `PLAYER` | Young person participating in the programme |
| `PARENT` | Parent/guardian (future: view child's progress) |
| `COACH` | Staff member who reviews submissions |
| `ADMIN` | Full system administrator |

### Permission Matrix

| Endpoint Group | PLAYER | PARENT | COACH | ADMIN |
|----------------|--------|--------|-------|-------|
| `POST /auth/login` | ✅ | ✅ | ✅ | ✅ |
| `POST /auth/logout` | ✅ | ✅ | ✅ | ✅ |
| `GET /auth/me` | ✅ | ✅ | ✅ | ✅ |
| `GET /me/profile` | ✅ | ✅ | ✅ | ✅ |
| `GET /me/badges` | ✅ | ✅ | ✅ | ✅ |
| `GET /me/modules` | ✅ | ✅ | ✅ | ✅ |
| `POST /me/challenges/{id}/submit` | ✅ | ✅ | ✅ | ✅ |
| `GET /leaderboards/centre` | ✅ | ✅ | ✅ | ✅ |
| `GET /leaderboards/global` | ✅ | ✅ | ✅ | ✅ |
| `GET /admin/centre/users` | ❌ | ❌ | ✅ | ✅ |
| `GET /admin/centre/submissions` | ❌ | ❌ | ✅ | ✅ |
| `POST /admin/submissions/*/approve` | ❌ | ❌ | ✅ | ✅ |
| `POST /admin/submissions/*/reject` | ❌ | ❌ | ✅ | ✅ |
| `/manage/*` (all CRUD) | ❌ | ❌ | ❌ | ✅ |

Roles are enforced using Jakarta `@RolesAllowed` annotations on each REST resource class. If a user attempts to access an endpoint they are not authorised for, they receive a `403 Forbidden` response.

---

## Centre Isolation

Centre isolation is a **core security principle** of this application. It ensures that data from one YMCA location cannot be seen or modified by users at another location.

### How It Works

- Every user belongs to exactly one centre (via `centre_id` foreign key)
- When a coach or admin queries users or submissions, the query is **automatically filtered** by their own `centre_id`
- The `AuthenticatedUserProvider` resolves the current user, and the resource classes use `admin.centre.id` to scope all queries
- There is no API parameter to "switch" centres — the centre is always determined by the logged-in user

### What Crosses Centre Boundaries

Only the **global leaderboard** (`GET /leaderboards/global`) shows data from all centres. This is read-only and only displays usernames, display names, and XP totals — no sensitive data.

### Enforcement Points

| Layer | How Centre Isolation is Enforced |
|-------|----------------------------------|
| Repository queries | `findByCentreId(admin.centre.id)` used in all admin queries |
| Submission review | Checks `submission.submittedBy.centre.id.equals(admin.centre.id)` before allowing approval/rejection |
| User management | `GET /manage/users` returns only users from the admin's centre |
| User creation | New users default to the admin's centre unless explicitly overridden |

---

## Password Security

### Hashing

Passwords are hashed using **bcrypt** (via Quarkus Elytron `BcryptUtil`):

- Passwords are **never stored in plain text**
- bcrypt includes a random salt, so identical passwords produce different hashes
- The default cost factor provides strong protection against brute-force attacks
- Hashing is handled by `PasswordUtil.hashPassword()` for user creation/update

### Password Policy

The application currently does **not enforce** password complexity rules (minimum length, special characters, etc.). This is a deliberate simplification for the hackathon. For production, consider adding validation in `UserResource.create()`.

### Password Reset

- There is **no self-service password reset** (no email addresses are stored)
- Only an ADMIN can reset a user's password via `PUT /manage/users/{id}` with a new `password` field
- If the only ADMIN account's password is lost, a manual database update is required (see [Support & Troubleshooting](support-and-troubleshooting.md#3-login-returns-401--invalid-credentials))

---

## Changing the Session Secret

The HMAC secret used to sign session cookies is defined in two places and **must be changed for production**:

### 1. Session Cookie Signing Secret

**File:** `src/main/java/.../security/SessionCookieUtil.java`

```java
private static final String SECRET = "wishaw-ymca-esports-secret-key-32chars!";
```

Change this to a unique random string (at least 32 characters). You can generate one with:

```bash
# Linux / Mac
openssl rand -base64 32

# PowerShell (Windows)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

> ⚠️ **If you change this secret after deployment, all existing session cookies will become invalid and users will need to log in again.** This is expected and safe — it's equivalent to "logging out all users".

### 2. Quarkus Session Encryption Key

**File:** `src/main/resources/application.properties`

```properties
quarkus.http.auth.session.encryption-key=wishaw-ymca-esports-secret-key-32chars!
```

Override this via environment variable in production:
```bash
export QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY="your-new-random-key-at-least-32-chars"
```

### Future Improvement

Ideally, the `SessionCookieUtil` secret should also be read from configuration rather than hardcoded. This is a recommended enhancement — inject it as a Quarkus `@ConfigProperty` so it can be changed without recompiling.

---

## Security Best Practices for Production

| # | Action | Priority |
|---|--------|----------|
| 1 | **Change the session HMAC secret** in `SessionCookieUtil.java` | 🔴 Critical |
| 2 | **Change the encryption key** in `application.properties` | 🔴 Critical |
| 3 | **Change all default passwords** (admin1, coach1, player1) | 🔴 Critical |
| 4 | **Enable HTTPS** via reverse proxy (Nginx/Caddy) or cloud load balancer | 🔴 Critical |
| 5 | **Add `Secure` flag** to session cookie (in `AuthResource.buildSetCookieHeader`) | 🔴 Critical |
| 6 | **Disable Swagger UI** (`quarkus.swagger-ui.always-include=false`) | 🟡 Recommended |
| 7 | **Disable SQL logging** (`quarkus.hibernate-orm.log.sql=false`) | 🟡 Recommended |
| 8 | **Restrict database file permissions** (only readable by the app user) | 🟡 Recommended |
| 9 | **Add password complexity validation** in `UserResource` | 🟢 Nice to have |
| 10 | **Add rate limiting** on the login endpoint to prevent brute-force attacks | 🟢 Nice to have |

---

## Known Limitations and Future Improvements

| Limitation | Impact | Suggested Improvement |
|------------|--------|----------------------|
| HMAC secret is hardcoded in Java source | Requires recompilation to change | Read from `@ConfigProperty` or environment variable |
| No session expiry beyond cookie Max-Age | Server cannot forcibly invalidate a session | Add a session store (database or in-memory cache) with server-side expiry |
| No password complexity rules | Weak passwords allowed | Add validation in `UserResource.create()` and `update()` |
| No rate limiting on login | Vulnerable to brute-force attacks | Add Quarkus rate-limiting extension or reverse proxy rate limiting |
| No audit log table | Admin actions not recorded for review | Add an `audit_log` table to track who did what and when |
| `Secure` cookie flag not set | Cookie sent over HTTP in development | Add the flag when deploying behind HTTPS |
| No CSRF token | SameSite=Strict mitigates most CSRF, but a token adds defence-in-depth | Add CSRF token for mutation endpoints |

