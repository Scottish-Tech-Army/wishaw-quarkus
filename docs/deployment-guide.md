# Deployment Guide

This guide covers how to build, deploy, and run the Wishaw YMCA eSports Badge Portal backend in production.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Option 1: Run Directly (Bare Metal / VM)](#option-1-run-directly-bare-metal--vm)
- [Option 2: Run with Docker](#option-2-run-with-docker)
- [Option 3: Cloud Deployment](#option-3-cloud-deployment)
- [Production Checklist](#production-checklist)
- [Updating the Application](#updating-the-application)
- [Rolling Back](#rolling-back)

---

## Prerequisites

| Requirement      | Minimum Version | Notes                                          |
|------------------|-----------------|-------------------------------------------------|
| Java (JDK)       | 17              | [Download from Adoptium](https://adoptium.net/) |
| Maven             | 3.8+            | Included via `mvnw` / `mvnw.cmd` wrapper       |
| Docker (optional) | 20+             | Only needed for containerised deployment        |

---

## Option 1: Run Directly (Bare Metal / VM)

This is the simplest deployment — ideal for a single server or low-budget setup.

### Step 1: Build the Application

**Windows:**
```cmd
mvnw.cmd package -DskipTests
```

**Mac / Linux:**
```bash
./mvnw package -DskipTests
```

This creates the runnable application in `target/quarkus-app/`.

### Step 2: Copy the Build Output to Your Server

Copy the entire `target/quarkus-app/` directory to your server. The directory structure must be kept intact:

```
quarkus-app/
├── quarkus-run.jar      ← main entry point
├── app/
├── lib/
└── quarkus/
```

### Step 3: Run the Application

```bash
java -jar quarkus-app/quarkus-run.jar
```

The API will start on port **8080** by default. To change the port:

```bash
java -Dquarkus.http.port=9090 -jar quarkus-app/quarkus-run.jar
```

### Step 4: Run as a Background Service (Linux)

Create a systemd service file at `/etc/systemd/system/wishaw-api.service`:

```ini
[Unit]
Description=Wishaw YMCA eSports API
After=network.target

[Service]
Type=simple
User=wishaw
WorkingDirectory=/opt/wishaw
ExecStart=/usr/bin/java -jar /opt/wishaw/quarkus-app/quarkus-run.jar
Restart=on-failure
RestartSec=10

# Environment overrides (see Configuration Reference)
Environment=QUARKUS_DATASOURCE_JDBC_URL=jdbc:h2:file:/opt/wishaw/data/wishaw;AUTO_SERVER=TRUE
Environment=QUARKUS_HTTP_PORT=8080

[Install]
WantedBy=multi-user.target
```

Then enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable wishaw-api
sudo systemctl start wishaw-api
sudo systemctl status wishaw-api
```

### Step 5: (Recommended) Put Behind a Reverse Proxy

Use Nginx or Caddy to handle HTTPS and forward traffic to the app:

**Nginx example** (`/etc/nginx/sites-available/wishaw`):
```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    ssl_certificate     /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Free HTTPS certificates are available via [Let's Encrypt](https://letsencrypt.org/) using `certbot`.

---

## Option 2: Run with Docker

### Step 1: Build the Application

```cmd
mvnw.cmd package -DskipTests
```

### Step 2: Build the Docker Image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t wishaw-api .
```

### Step 3: Run the Container

```bash
docker run -d \
  --name wishaw-api \
  -p 8080:8080 \
  -v /path/to/data:/deployments/data \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:h2:file:./data/wishaw;AUTO_SERVER=TRUE" \
  wishaw-api
```

> **Important:** The `-v` flag mounts a host directory for the database. This ensures your data persists even if the container is replaced. Without this, **you will lose all data** when the container is removed.

### Docker Compose Example

Create a `docker-compose.yml`:

```yaml
version: '3.8'
services:
  api:
    build:
      context: .
      dockerfile: src/main/docker/Dockerfile.jvm
    ports:
      - "8080:8080"
    volumes:
      - ./data:/deployments/data
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: "jdbc:h2:file:./data/wishaw;AUTO_SERVER=TRUE"
    restart: unless-stopped
```

Run with:
```bash
docker compose up -d
```

---

## Option 3: Cloud Deployment

The application can be deployed to any cloud platform that supports Java or Docker.

### Recommended Low-Cost Options

| Platform                  | Cost              | Notes                                          |
|---------------------------|-------------------|-------------------------------------------------|
| **Railway.app**           | Free tier available | Supports Docker, easy setup                    |
| **Render.com**            | Free tier available | Supports Docker, auto-deploy from GitHub       |
| **Oracle Cloud Free Tier**| Always free VM    | Run as bare-metal with systemd                  |
| **Fly.io**                | Free tier available | Supports Docker, global edge deployment        |
| **Azure App Service**     | Free tier (F1)    | Supports Java directly                          |

### General Cloud Steps

1. **Build** the application (`mvnw package -DskipTests`)
2. **Push** the Docker image to a container registry (Docker Hub, GitHub Container Registry, etc.)
3. **Deploy** the image to your cloud provider
4. **Mount persistent storage** for the `data/` directory (critical for H2 database)
5. **Set environment variables** for production configuration (see [Production Checklist](#production-checklist))

---

## Production Checklist

Before going live, complete every item below:

### 1. Change the Session Secret Key

The default key in `application.properties` is **not secure**. Override it with a random 32+ character string:

```bash
# Generate a random key (Linux/Mac)
openssl rand -base64 32
```

Set it via environment variable:
```bash
export QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY="your-random-32-char-key-here!!!"
```

You must also update the HMAC secret in `SessionCookieUtil.java` (see [Security Model](security-model.md#changing-the-session-secret)).

### 2. Change Default Passwords

Log in as `admin1` (password: `password123`) and use the user management API to update all passwords. Alternatively, create new admin accounts and deactivate the defaults.

### 3. Remove or Isolate Seed Data

The test users from migration `V1.0.2` are intended for development only. For production, either:
- Change their passwords immediately after first deployment, or
- Create a new migration that deletes the test accounts

### 4. Secure the Database File

The H2 database is stored as a file on disk (`data/wishaw.mv.db`). Ensure:
- The file is only readable by the application user
- Regular backups are taken (see [Database & Data Integrity](database-and-data-integrity.md#backups))
- The directory is on persistent storage (not ephemeral container storage)

### 5. Enable HTTPS

Never run the API over plain HTTP in production. Use a reverse proxy (Nginx, Caddy) with HTTPS, or deploy behind a cloud load balancer that terminates TLS.

### 6. Set the Cookie Secure Flag

When running behind HTTPS, update the cookie builder in `AuthResource.java` to add `Secure;` to the `Set-Cookie` header so cookies are only sent over HTTPS.

### 7. Disable Swagger UI in Production

In `application.properties`, Swagger UI is set to always show. For production, override:
```
quarkus.swagger-ui.always-include=false
```

### 8. Disable SQL Logging

```
quarkus.hibernate-orm.log.sql=false
```

---

## Updating the Application

### Step 1: Pull the Latest Code

```bash
git pull origin main
```

### Step 2: Review Database Migrations

Check `src/main/resources/db/migration/` for any new `V*.sql` files. Flyway applies these automatically on startup — you do not need to run them manually.

### Step 3: Rebuild

```cmd
mvnw.cmd package -DskipTests
```

### Step 4: Restart the Application

**Bare metal (systemd):**
```bash
sudo systemctl restart wishaw-api
```

**Docker:**
```bash
docker compose down
docker compose up -d --build
```

### Step 5: Verify

- Check the application logs for any migration errors
- Hit the health endpoint or Swagger UI to confirm the API is responding
- Test a login with a known account

---

## Rolling Back

### Application Rollback

If an update causes problems:

1. **Stop** the application
2. **Deploy the previous version** (keep old JAR files or Docker images tagged by version)
3. **Restart**

### Database Rollback

Flyway migrations are **forward-only** by design. If a migration causes issues:

1. **Restore from backup** (see [Database & Data Integrity](database-and-data-integrity.md#restoring-from-backup))
2. Fix the migration script
3. Re-deploy

> ⚠️ **Always back up the database before deploying updates.** A simple file copy of `data/wishaw.mv.db` while the app is stopped is sufficient.

