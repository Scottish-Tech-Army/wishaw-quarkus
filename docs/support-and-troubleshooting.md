# Support & Troubleshooting

This guide helps you diagnose and fix common issues, monitor the application, and perform routine maintenance.

---

## Table of Contents

- [Checking if the App is Running](#checking-if-the-app-is-running)
- [Viewing Logs](#viewing-logs)
- [Common Issues and Fixes](#common-issues-and-fixes)
- [Routine Maintenance](#routine-maintenance)
- [Performance Considerations](#performance-considerations)
- [Getting Help](#getting-help)

---

## Checking if the App is Running

### Quick Health Check

Open a browser or use `curl` to hit the Swagger UI:

```
http://localhost:8080/q/swagger-ui
```

If the page loads, the API is running.

### Check from the Command Line

**Windows:**
```cmd
curl http://localhost:8080/q/openapi
```

**Linux / Mac:**
```bash
curl -s http://localhost:8080/q/openapi | head -5
```

A successful response returns the OpenAPI spec in JSON/YAML.

### Check the Systemd Service (Linux)

```bash
sudo systemctl status wishaw-api
```

### Check the Docker Container

```bash
docker ps                          # Is the container running?
docker logs wishaw-api --tail 50   # Last 50 log lines
```

---

## Viewing Logs

### Dev Mode

When running with `mvnw quarkus:dev`, logs are printed directly to the console.

### Production (Bare Metal)

Logs go to standard output. If running as a systemd service:

```bash
# View live logs
sudo journalctl -u wishaw-api -f

# View last 100 lines
sudo journalctl -u wishaw-api -n 100

# View logs from today
sudo journalctl -u wishaw-api --since today
```

### Production (Docker)

```bash
# View live logs
docker logs -f wishaw-api

# View last 100 lines
docker logs --tail 100 wishaw-api
```

### What to Look For

| Log Message | Meaning |
|-------------|---------|
| `Listening on: http://0.0.0.0:8080` | App started successfully |
| `Flyway migration V1.0.X applied` | Database migration ran successfully |
| `Flyway migration failed` | A database migration has an error — see details below |
| `java.sql.SQLException` | Database connection or query error |
| `401 Unauthorized` | A request was made without valid authentication |
| `403 Forbidden` | A user tried to access a resource they don't have permission for |

---

## Common Issues and Fixes

### 1. Application Won't Start — "Flyway migration checksum mismatch"

**Cause:** A migration file was modified after it was already applied to the database.

**Fix:**
- **Option A:** Restore the original migration file (check git history: `git log --oneline src/main/resources/db/migration/`)
- **Option B:** Restore the database from backup and re-apply
- **Option C (dev only):** Delete the `data/` folder to start fresh — **this deletes all data**

---

### 2. Application Won't Start — "Port 8080 already in use"

**Cause:** Another application is using port 8080.

**Fix:**
- Stop the other application, or
- Change the port:
  ```bash
  java -Dquarkus.http.port=9090 -jar quarkus-app/quarkus-run.jar
  ```

---

### 3. Login Returns 401 — "Invalid credentials"

**Cause:** Wrong username or password.

**Fix:**
- Double-check the username (case-sensitive)
- If you've forgotten the password, an ADMIN must reset it via the user management API:
  ```
  PUT /manage/users/{userId}
  { "password": "new-password" }
  ```
- If the ADMIN password is lost, you can reset it directly in the database (requires stopping the app and using the H2 console or a database tool). Generate a new bcrypt hash using an online tool or the `PasswordUtil` class.

---

### 4. Database File Locked — "The file is locked"

**Cause:** Two instances of the application are trying to access the same database file, or the app crashed without releasing the lock.

**Fix:**
1. Stop all instances of the application
2. Delete the lock file: `data/wishaw.lock.db`
3. Restart the application

---

### 5. Data Not Showing After Restart

**Cause:** If running in Docker without a volume mount, the database is stored inside the container and is lost when the container is removed.

**Fix:**
- Always use a volume mount for the `data/` directory:
  ```bash
  docker run -v /path/on/host/data:/deployments/data wishaw-api
  ```
- See the [Deployment Guide](deployment-guide.md#option-2-run-with-docker) for details.

---

### 6. CORS Errors in the Browser

**Cause:** The frontend is running on a different domain/port than the API.

**Fix:** Add CORS configuration to `application.properties`:
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.headers=Content-Type
quarkus.http.cors.access-control-allow-credentials=true
```

---

### 7. "Out of Memory" Errors

**Cause:** The JVM doesn't have enough memory allocated.

**Fix:** Increase the heap size:
```bash
java -Xmx512m -jar quarkus-app/quarkus-run.jar
```

For Docker, set the memory limit:
```bash
docker run -m 512m wishaw-api
```

---

### 8. Swagger UI Not Loading in Production

**Cause:** Swagger UI may be disabled in production by default or by configuration override.

**Fix:** Ensure `quarkus.swagger-ui.always-include=true` is set (note: consider security implications of exposing API docs publicly).

---

## Routine Maintenance

### Weekly

- [ ] **Check logs** for any errors or warnings
- [ ] **Verify backups** are being created (see [Database & Data Integrity](database-and-data-integrity.md#backups))

### Monthly

- [ ] **Test a backup restore** on a separate machine to ensure backups are valid
- [ ] **Review user accounts** — deactivate any that are no longer needed
- [ ] **Check disk space** — ensure the database file and backups aren't filling up the disk

### Before Each Update

- [ ] **Back up the database** (copy `data/wishaw.mv.db`)
- [ ] **Read the changelog** for any breaking changes
- [ ] **Test the update** in a development environment first

---

## Performance Considerations

### Current Design

The XP calculation is done **on-the-fly** — every time badges or leaderboards are requested, the system re-calculates XP from all approved submissions. This is:

- **Always accurate** — no stale data
- **Simple** — no cache invalidation logic
- **May slow down** with thousands of users and submissions

### When to Optimise

If you notice the leaderboard or badge endpoints becoming slow (>2 seconds), consider:

1. **Adding database indexes** on frequently queried columns (many are already in place)
2. **Caching XP totals** in a separate table, updated when submissions are approved/rejected
3. **Migrating to PostgreSQL** which handles larger datasets more efficiently than H2
4. **Adding pagination** to leaderboard endpoints

For the expected scale (a handful of YMCA centres with dozens of players each), the current design should perform well for years.

---

## Getting Help

### Application Logs

Always include relevant log output when reporting issues. The most useful information is:

1. The full error message and stack trace
2. What action triggered the error (which API endpoint, what request body)
3. The timestamp of when it happened

### Useful Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Quarkus FAQ](https://quarkus.io/faq/)
- [H2 Database Documentation](https://h2database.com/html/main.html)
- [Flyway Documentation](https://documentation.red-gate.com/flyway)

### Contact

Refer to the project's CODEOWNERS file or original project handover documentation for technical contacts.
