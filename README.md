# Film Review Platform - Backend

Backend API for the Film Review Platform built with Spring Boot 3.x, Java 17, PostgreSQL, and Redis.

## Tech Stack

- **Framework**: Spring Boot 3.3.0
- **Language**: Java 17+
- **Database**: PostgreSQL 15+
- **ORM**: Spring Data JPA / Hibernate
- **Cache**: Redis
- **Migrations**: Liquibase
- **Security**: Spring Security + JWT
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Gradle

## Prerequisites

- Java 17 or higher
- Gradle 8.0+ (or use Gradle Wrapper)
- PostgreSQL 15+
- Redis (optional for local development)

## Getting Started

### 1. Clone and Setup

```bash
cd backend
```

### 2. Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE filmreview;
```

Or use environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/filmreview
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
```

### 3. Configure Redis (Optional)

For local development, Redis is optional. Set environment variables:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 4. Run the Application

```bash
# Using Gradle Wrapper (recommended)
./gradlew bootRun

# Or using Gradle directly
gradle bootRun

# Or build and run
./gradlew build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 5. Access API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/filmreview/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access (JPA)
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── config/        # Configuration classes
│   │   │   ├── exception/      # Exception handlers
│   │   │   └── security/      # Security configuration
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/changelog/    # Liquibase changelogs
│   └── test/
└── build.gradle
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/filmreview` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | (empty) |
| `JWT_SECRET` | JWT signing secret | (required in production) |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token expiration (ms) | `900000` (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7 days) |
| `TMDB_API_KEY` | TMDB API key | (required) |
| `PORT` | Server port | `8080` |

## Development

### Running Tests

```bash
./gradlew test
```

### Building

```bash
./gradlew build
```

### Database Migrations

Migrations are managed by Liquibase and located in `src/main/resources/db/changelog/`.

The master changelog file is `db.changelog-master.xml`, which includes individual changelog files.

Changelog files can be:
- XML format: `db/changelog/changes/V1__Initial_schema.xml`
- SQL format: `db/changelog/changes/V1__Initial_schema.sql`
- YAML format: `db/changelog/changes/V1__Initial_schema.yaml`
- JSON format: `db/changelog/changes/V1__Initial_schema.json`

Example XML changelog:
```xml
<databaseChangeLog>
    <changeSet id="1" author="developer">
        <createTable tableName="users">
            <column name="id" type="uuid">
                <constraints primaryKey="true"/>
            </column>
            <column name="username" type="varchar(50)">
                <constraints nullable="false" unique="true"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

To include a changelog in the master file:
```xml
<include file="db/changelog/changes/V1__Initial_schema.xml"/>
```

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

See `docs/API_DESIGN.md` for complete API documentation.

## Development Profile

Run with development profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

This enables:
- SQL logging
- Hibernate DDL auto-update
- Debug logging

## Production Profile

Run with production profile:

```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## License

MIT
# film-review-api
