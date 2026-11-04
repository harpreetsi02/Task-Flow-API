# TaskFlow API

A multi-tenant task management SaaS backend built with **Spring Boot**. Teams create organizations, manage projects and tasks, receive webhooks when work is completed, and integrate programmatically using API keys, all behind JWT, OAuth 2.0 and role-based access control.

> Built as a learning project to practice production-style backend patterns: stateless authentication, resource-level authorization, tenant isolation, reliable event delivery and API evolution.

---

## Features

### Authentication and Security
- **Register / Login** with BCrypt password hashing
- **JWT access tokens** (15 min) and **DB-tracked refresh tokens** (7 days)
- **Refresh token rotation**: every refresh revokes the old token and issues a new pair; reusing a revoked token is rejected
- **Logout** that blacklists the access token and revokes the refresh token
- **Google OAuth 2.0 login** that resolves to the same JWT pair as normal login (find-or-create by email)
- **API keys** for machine-to-machine access (`X-API-Key`), see below

### Multi-tenancy and Authorization
- **Organizations** with auto-generated unique slugs
- **Memberships** with per-organization roles: `OWNER`, `ADMIN`, `MEMBER` (separate from the global platform role)
- Centralized, reusable permission checks (`OrganizationAuthorizationService`)
- **Tenant isolation**: every lookup is scoped (`findByIdAndOrganization`, `findByIdAndProject`) so one organization can never reach another's data

### Projects and Tasks
- Full CRUD for projects (role-based) and tasks
- Task assignment validated against organization membership
- Flexible status flow (`TODO`, `IN_PROGRESS`, `DONE`), priority, due date
- Pagination and optional `?status=` filtering

### Webhooks
- Per-organization webhook subscriptions (`TASK_COMPLETED`), with pause/resume and delete
- **Transactional outbox**: deliveries are written in the same transaction as the task update
- **Scheduled delivery worker** with **exponential backoff** (1, 5, 30, 120 minutes; max 5 attempts)
- `4xx` responses are treated as permanent failures, `5xx` and timeouts are retried
- Paused webhooks keep their pending deliveries until resumed
- Connect and read timeouts on outgoing calls

### API Versioning
- URI versioning: `/api/v1` and `/api/v2`
- v2 Tasks return `createdBy` as an object instead of v1's `createdByName` string
- Service layer is version-agnostic; versions differ only in controller, DTO and mapper
- `Deprecation` and `Sunset` headers on v1 task endpoints

### API Keys
- Keys are generated with `SecureRandom` (256-bit) and **shown only once**
- Stored as **SHA-256 hashes** with a short display prefix; revocation is permanent
- Keys are **bound to one organization** and act as the user who created them
- Deny-by-default path check: a key can only reach `/api/vN/organizations/{its own org}/**` and can never manage API keys
- `lastUsedAt` is updated at most once every 5 minutes to avoid a write per request

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3, Spring Web |
| Security | Spring Security, JWT (jjwt), OAuth 2.0 Client |
| Persistence | Spring Data JPA / Hibernate, MySQL or MariaDB |
| Async and Scheduling | `@Scheduled`, `RestTemplate` |
| Build | Maven |

No Lombok: entities, DTOs and mappers are written by hand (DTOs use Java records).

---

## Roles and Permissions

| Action | OWNER | ADMIN | MEMBER |
|---|:---:|:---:|:---:|
| View projects and tasks | yes | yes | yes |
| Create / update / delete tasks | yes | yes | yes |
| Create / update projects | yes | yes | no |
| Delete projects | yes | no | no |
| Invite members | yes | yes | no |
| Manage webhooks | yes | yes | no |
| Manage API keys | yes | yes | no |

---

## API Overview

Base path: `/api/v1` (Tasks are also available under `/api/v2`).

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login, returns access and refresh tokens |
| POST | `/auth/refresh` | Rotate refresh token |
| POST | `/auth/logout` | Blacklist access token, revoke refresh token |
| GET | `/oauth2/authorization/google` | Start Google login (browser) |

### Organizations
| Method | Endpoint | Description |
|---|---|---|
| POST | `/organizations` | Create an organization (creator becomes OWNER) |
| GET | `/organizations` | List my organizations with my role |
| POST | `/organizations/{orgId}/members` | Invite a member |

### Projects
| Method | Endpoint |
|---|---|
| POST / GET | `/organizations/{orgId}/projects` |
| PUT / DELETE | `/organizations/{orgId}/projects/{projectId}` |

### Tasks (v1 and v2)
| Method | Endpoint |
|---|---|
| POST / GET | `/organizations/{orgId}/projects/{projectId}/tasks` |
| PUT / DELETE | `/organizations/{orgId}/projects/{projectId}/tasks/{taskId}` |

`GET` supports `?status=TODO|IN_PROGRESS|DONE&page=0&size=10`.

### Webhooks
| Method | Endpoint | Description |
|---|---|---|
| POST / GET | `/organizations/{orgId}/webhooks` | Create / list |
| PATCH | `/organizations/{orgId}/webhooks/{id}` | Pause or resume (`{"active": false}`) |
| DELETE | `/organizations/{orgId}/webhooks/{id}` | Delete |

### API Keys
| Method | Endpoint | Description |
|---|---|---|
| POST | `/organizations/{orgId}/api-keys` | Create (raw key returned once) |
| GET | `/organizations/{orgId}/api-keys` | List (prefix only) |
| DELETE | `/organizations/{orgId}/api-keys/{id}` | Revoke |

---

## Getting Started

### Prerequisites
- Java 17 or newer
- Maven
- MySQL or MariaDB
- (Optional) Google Cloud OAuth credentials for Google login

### 1. Create the database
```sql
CREATE DATABASE taskflow_db;
```

### 2. Configure `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/taskflow_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

app.jwt.secret=CHANGE_ME_TO_A_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS
app.jwt.access-token-expiration=900000
app.jwt.refresh-token-expiration=604800000

spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile

app.api.v1.sunset-date=Wed, 30 Jun 2027 23:59:59 GMT
```
Do not commit real secrets. Prefer environment variables in any shared or deployed setup.

For Google login, register this redirect URI in Google Cloud Console:
`http://localhost:8080/login/oauth2/code/google`

### 3. Run
```bash
mvn spring-boot:run
```
The API starts on `http://localhost:8080`.

---

## Example Usage

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"rahul@example.com","password":"Test@1234","fullName":"Rahul Sharma"}'

# Login (returns accessToken and refreshToken)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"rahul@example.com","password":"Test@1234"}'

# Create an organization
curl -X POST http://localhost:8080/api/v1/organizations \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Acme Corp"}'

# Create an API key for that organization (raw key is shown only once)
curl -X POST http://localhost:8080/api/v1/organizations/1/api-keys \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"name":"CI integration"}'

# Use the API key
curl http://localhost:8080/api/v1/organizations/1/projects \
  -H "X-API-Key: <rawKey>"
```

Webhook payload sent on task completion:
```json
{
  "event": "TASK_COMPLETED",
  "taskId": 12,
  "taskTitle": "Design homepage",
  "projectId": 3
}
```

---

## Project Structure

```
src/main/java/com/backend/taskflowapi
├── config          # Security, Web (interceptors), RestTemplate
├── controller      # REST controllers (v1 and v2)
├── dto
│   ├── request
│   └── response
├── entity          # JPA entities
├── enums
├── exception       # Custom exceptions and global handler
├── mapper          # Entity -> neutral result -> versioned response
├── repository      # Spring Data repositories
├── security        # JWT, filters, API key generator, OAuth2 handler
└── service         # Business logic (version-agnostic)
```

---

## Design Notes

- **Two kinds of roles**: a global platform role on `User`, and an organization role on `Membership`.
- **Join entity with data**: `Membership` is a manual bridge entity because it carries a role.
- **Passwords vs API keys**: passwords use slow salted BCrypt; API keys are high-entropy random values, so a fast SHA-256 hash is enough and allows direct lookup by hash.
- **Outbox pattern**: the webhook delivery row is committed with the business change, so an event is never sent for a rolled-back transaction and survives restarts.
- **No slow network calls inside a database transaction**: the delivery worker saves each delivery in its own short transaction.
- **Fail-safe defaults**: API keys are denied everywhere except explicitly allowed paths.

---

## Known Limitations

- The access token blacklist is **in-memory**; with multiple instances it should move to a shared store such as Redis.
- The webhook worker assumes a **single instance**; multiple instances would need row locking (`SKIP LOCKED`) or a scheduler lock.
- Webhook payloads are not yet signed (HMAC) and have no event ID for receiver-side idempotency.
- Secrets live in `application.properties`; use environment variables or a secret manager in production.
- `ddl-auto=update` is used for convenience; a migration tool (Flyway or Liquibase) is recommended for real deployments.
- Verified manually with Postman; automated tests are not included yet.

## Roadmap

- Rate limiting / throttling (`429`, `Retry-After`)
- OpenAPI / Swagger documentation
- API gateway
- Webhook signature verification and delivery log endpoint
- Automated unit and integration tests