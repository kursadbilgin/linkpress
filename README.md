# LinkPress - URL Shortener Service

A URL shortener service built with Spring Boot that generates short links, handles redirects, and tracks click counts.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Maven
- Lombok
- Jakarta Validation
- JUnit 5 / Mockito
- Springdoc OpenAPI (Swagger)
- Docker

## Architecture

```
src/main/java/com/kursad/linkpress
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/            # Request/Response models
├── exception/      # Custom exceptions & global handler
├── mapper/         # Entity <-> DTO conversion
├── config/         # Spring configuration
└── util/           # Short code generator
```

## API Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/v1/urls` | Create short URL | 201 |
| GET | `/api/v1/urls` | List all URLs | 200 |
| GET | `/api/v1/urls/{shortCode}` | Get URL details | 200 |
| PATCH | `/api/v1/urls/{shortCode}/deactivate` | Deactivate URL | 204 |
| DELETE | `/api/v1/urls/{shortCode}` | Delete URL | 204 |
| GET | `/{shortCode}` | Redirect to original URL | 302 |

## Quick Start

### Prerequisites

- Java 17
- Docker & Docker Compose

### Run with Docker Compose

```bash
docker compose up -d
```

The app will be available at `http://localhost:8080`.

### Run Locally

1. Start PostgreSQL:
```bash
docker compose up -d postgres
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

### API Documentation

Once the app is running, visit: `http://localhost:8080/swagger-ui.html`

## Usage Examples

### Create a short URL
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com/kursadbilgin"}'
```

### Create with custom alias
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://github.com/kursadbilgin", "customAlias": "kursad"}'
```

### Redirect
```bash
curl -L http://localhost:8080/kursad
```

### List all URLs
```bash
curl http://localhost:8080/api/v1/urls
```

## Running Tests

```bash
./mvnw test
```

Tests use H2 in-memory database - no PostgreSQL required.

## Business Rules

- Original URL is required and must be a valid HTTP/HTTPS URL
- Custom alias must be 3-50 characters (letters, numbers, hyphens, underscores)
- Expiration date must be in the future
- Inactive URLs cannot be redirected (403)
- Expired URLs cannot be redirected (410)
- Duplicate short codes are not allowed (409)
- Click count is incremented on each successful redirect
