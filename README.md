# Auth Service

Small Spring Boot authentication service for microservices using JWTs.

## Key Features
- Register and authenticate users (username, email, and password)
- Passwords hashed with BCrypt before storage
- Issues signed JWTs (HS256) for authentication, valid for **24 hours**
- Spring Security integration with a custom JWT authentication filter
- Role-based access control (`ADMIN`, `USER`)
- PostgreSQL persistence with Spring Data JPA (Hibernate)
- Docker + Docker Compose support
- SpringDoc OpenAPI (Swagger UI)

## Tech Stack
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven
- [JJWT](https://github.com/jwtk/jjwt)
- Docker / Docker Compose

## Quick Start with Docker Compose
This repo includes a `docker-compose.yml` that starts the `auth-service` and a `postgres` container. The service reads configuration from environment variables (via a project `.env`).

### 1) Create a `.env` in the project root
Create or edit `.env` with these values:

```dotenv
POSTGRES_DB=authdb
POSTGRES_USER=authuser
POSTGRES_PASSWORD=authpassword

# Base64-encoded 32-byte (256-bit) key for HS256 (replace with generated value)
SECRET_KEY=REPLACE_WITH_BASE64_32_BYTE_KEY
```

Important:
- `SECRET_KEY` must be a standard Base64 string (not Base64URL) and decode to 32 bytes (256 bits) for HS256.
- Do NOT leave `SECRET_KEY` empty.

### 2) Generate a valid `SECRET_KEY` (Linux / macOS / WSL)
Run this command and paste the output into `.env` for `SECRET_KEY`:

```bash
openssl rand -base64 32
```

### 3) Start the services
Run from the project root:

```bash
docker compose up --build
```

The compose file exposes the auth service on `http://localhost:8080`.

## Authentication Flow
1. Call `POST /auth/initialize` once to create the first admin account.
2. `POST /auth/login` with credentials.
3. Receive JWT in the `Authorization` response header (`Bearer <token>`).
4. Include `Authorization: Bearer <token>` when calling protected endpoints.
5. `GET /auth/verify` validates a token and returns user metadata (for other microservices).

## API Endpoints

### Public (`/auth/**`)

#### `POST /auth/initialize`
Creates the first admin account. Returns `401` if any admin already exists.

Request:
```json
{
  "email": "you@example.com",
  "username": "admin",
  "password": "password",
  "role": "ADMIN"
}
```
Response: `200 OK` with user metadata, or `401 Unauthorized` if an admin already exists.

---

#### `POST /auth/login`
Authenticates a user and issues a JWT.

Request:
```json
{
  "username": "user",
  "password": "password"
}
```
Response:
- `200 OK` with user metadata in the response body.
- JWT returned in `Authorization: Bearer <token>` response header.
- `401 Unauthorized` on invalid credentials.

---

#### `GET /auth/verify`
Validates a JWT. Intended for use by other microservices.

Request header: `Authorization: Bearer <token>`

Response:
- `200 OK` with the user's metadata if the token is valid.
- `401 Unauthorized` if the token is missing, invalid, or the user cannot be found.

---

### Admin Only (`/admin/**` — requires `ROLE_ADMIN`)

#### `POST /admin/register`
Creates a new user account.

Request:
```json
{
  "email": "user@example.com",
  "username": "user",
  "password": "password",
  "role": "USER"
}
```
Response: `200 OK` with the created user's metadata.

---

#### `GET /admin/ping`
Health check for admin-authenticated clients.

Response: `200 OK` — `pong`

---

### User (`/user/**` — requires `ROLE_USER` or `ROLE_ADMIN`)

#### `GET /user/ping`
Health check for authenticated clients.

Response: `200 OK` — `pong`

---

## Swagger UI
Interactive API docs are available at:

`http://localhost:8080/swagger-ui.html`

To test protected endpoints in Swagger:
1. Log in via `/auth/login` to obtain a token.
2. Click the **Authorize** button and enter `Bearer <your-token>`.
3. Swagger will send the `Authorization` header with requests.
