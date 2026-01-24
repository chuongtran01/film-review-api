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
- Docker and Docker Compose (for local development services)
- PostgreSQL 15+ (or use Docker Compose)
- Redis (optional for local development, or use Docker Compose)

## Getting Started

### 1. Clone and Setup

```bash
cd backend
```

### 2. Start Services with Docker Compose

The easiest way to start PostgreSQL and Redis for local development is using Docker Compose:

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Check services are running
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

This will start:
- **PostgreSQL** on port `5432`
  - Database: `filmreview_dev`
  - Username: `devuser`
  - Password: `devpassword`
- **Redis** on port `6379`

The services are configured to match the default values in `application-dev.yml`.

### Alternative: Manual Database Setup

If you prefer to set up PostgreSQL and Redis manually:

**PostgreSQL:**
```sql
CREATE DATABASE filmreview_dev;
```

Or use environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/filmreview_dev
export DATABASE_USERNAME=devuser
export DATABASE_PASSWORD=devpassword
```

**Redis:**
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 3. Environment Variables Setup

Create a `.env` file in the `backend` directory with your environment variables:

```bash
# .env file example
DATABASE_URL=jdbc:postgresql://localhost:5432/filmreview_dev
DATABASE_USERNAME=devuser
DATABASE_PASSWORD=devpassword
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-secret-key-here
TMDB_API_KEY=your-tmdb-api-key-here
```

### 4. Run the Application

**Option 1: Using .env file with export (Linux/Mac)**
```bash
# Load environment variables from .env file
export $(cat .env | xargs)

# Then run the application
./gradlew bootRun
```

**Option 2: Using .env file inline (Linux/Mac)**
```bash
# Load and run in one command
export $(cat .env | xargs) && ./gradlew bootRun
```

**Option 3: Using env-cmd (cross-platform, requires npm)**
```bash
# Install env-cmd globally (one time)
npm install -g env-cmd

# Run with .env file
env-cmd ./gradlew bootRun
```

**Option 4: Direct execution (without .env file)**
```bash
# Using Gradle Wrapper (recommended)
./gradlew bootRun

# Or using Gradle directly
gradle bootRun

# Or build and run
./gradlew build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

**Note**: If you don't use a `.env` file, make sure to export environment variables in your shell or set them in your IDE run configuration.

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
