# Auth Service

Small Spring Boot authentication service for microservices using JWTs.

## Key Features
- Register and authenticate users (username, email, and password)
- Passwords hashed with BCrypt before storage
- Issues signed JWTs (HS256) for authentication, valid for **24 hours**
- Spring Security integration with a custom JWT authentication filter
- Role-based access control (`ADMIN`, `USER`)
- Simple User/Admin domain model
- Uses Spring Data JPA (Hibernate) — persistence layer is swappable (H2, Postgres, MySQL, etc.)

## Tech Stack
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven
- [JJWT](https://github.com/jwtk/jjwt) for JWT creation and validation
- SpringDoc OpenAPI (Swagger UI)

## Quick Start

1. Build the project:
   ```powershell
   mvn clean package
   ```

2. Run locally:
   ```powershell
   mvn spring-boot:run
   ```

## Configuration

Configuration lives in `src/main/resources/application.properties`. The app uses PostgreSQL.

### Datasource Properties

| Property | Value                                     | Description |
|---|-------------------------------------------|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/authdb` | PostgreSQL connection string (host, port, database name) |
| `spring.datasource.username` | `admin`                                   | Database user |
| `spring.datasource.password` | `password`                                | Database password |
**Example connection string breakdown:**
```
jdbc:postgresql://localhost:5432/authdb
                 ^        ^    ^
                 |        |    +-- database name
                 |        +------- port (default 5432)
                 +------- hostname (localhost for local dev)
```

## Authentication Flow

1. Call `/auth/initialize` once to create the first admin account.
2. `POST /auth/login` with credentials → receive a JWT in the `Authorization` response header and as an `AUTH_TOKEN` cookie.
3. Include the token in subsequent requests as `Authorization: Bearer <token>`.
4. Protected endpoints validate the token via the `JwtAuthFilter`.
5. Call `POST /auth/logout` to clear the `AUTH_TOKEN` cookie.
6. Other services can validate tokens by calling `GET /auth/verify`.

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
- JWT also set as `Set-Cookie: AUTH_TOKEN=<token>; HttpOnly; Secure; Path=/; Max-Age=86400`.
- `401 Unauthorized` on invalid credentials.

---

#### `POST /auth/logout`
Clears the `AUTH_TOKEN` cookie by setting it with `Max-Age=0`.

Response: `200 OK`.

> Note: The JWT itself remains technically valid until it expires. This endpoint only clears the cookie.

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

Interactive API docs are available at `http://localhost:8080/swagger-ui.html` when the application is running.

To test protected endpoints:
1. Call `/auth/login` or `/auth/initialize` to get a token.
2. Click the **Authorize** button at the top of the Swagger UI.
3. Enter `Bearer <your-token>` in the `bearerAuth` field.
4. Protected endpoints will now include the `Authorization` header automatically.