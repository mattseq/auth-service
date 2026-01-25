# Auth Service

Small Spring Boot authentication service using JWTs.

## Key features
- Register and authenticate users (email + password)
- Passwords stored securely using hashing
- Issues signed JWTs for authentication
- Spring Security integration with a JWT authentication filter
- Simple User and Role domain model
- Uses Spring Data JPA so the persistence layer is swappable (H2, Postgres, MySQL, etc.)

## Tech stack
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)
- Maven
- io.jsonwebtoken (JJWT) for JWT creation and validation

## Quick start
1. Build the project:

   ```powershell
   mvn clean package
   ```

2. Run locally:

   ```powershell
   mvn spring-boot:run
   ```

## Configuration
- Primary configuration lives in `src/main/resources/application.properties`.
- The application uses Spring Data JPA - switch databases by changing the JDBC URL, driver, username, and password in the properties file.

## Typical API endpoints
- POST `/auth/initialize`
    - Request: JSON (CreateUserRequest)
      ```json
      {
        "email": "you@example.com",
        "username": "admin",
        "password": "password",
        "role": "ADMIN" // optional, defaults to ADMIN if omitted
      }
      ```
    - Response:
        - `200 OK` with user metadata when an initial admin is created.
        - `401 Unauthorized` when an admin already exists (no user created).
- 
- POST `/admin/register`
    - Request: JSON (CreateUserRequest)
      ```json
      {
        "email": "user@example.com",
        "username": "user",
        "password": "password",
        "role": "USER" // or ADMIN
      }
      ```
    - Response:
        - `200 OK` with the created user's metadata in the response body.
        - Note: this endpoint is protected (only admins should be able to call it).

- POST `/auth/login`
    - Request: JSON (LoginRequest)
      ```json
      {
        "username": "user",
        "password": "password"
      }
      ```
    - Response:
        - `200 OK` with the authenticated user's metadata in the response body.
        - The JWT is returned in the response header `Authorization: Bearer <token>` and as a cookie `Set-Cookie: AUTH_TOKEN=<token>; HttpOnly; Secure`.
        - `401 Unauthorized` on invalid credentials.

- GET `/auth/verify`
    - Requires header: `Authorization: Bearer <token>`
    - Response:
        - `200 OK` with the user's metadata if the token is valid. 
        - `401 Unauthorized` if the token is missing/invalid or the user cannot be found.
  }
}