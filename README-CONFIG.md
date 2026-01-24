# Configuration Guide

Spring Boot automatically reads environment variables. Here are the best ways to configure your API keys and secrets for local development:

## Option 1: Use `application-dev.yml` (Recommended)

1. Copy the example file:
   ```bash
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
   ```

2. Fill in your values in `application-dev.yml`:
   ```yaml
   tmdb:
     api-key: your-actual-api-key-here
   
   jwt:
     secret: your-jwt-secret-here
   ```

3. Run the app (defaults to `dev` profile):
   ```bash
   ./gradlew bootRun
   ```

   The `dev` profile is set as default in `application.yml`, so no need to specify it.

## Option 2: Use Environment Variables

Spring Boot automatically reads environment variables. Set them in your shell:

**On macOS/Linux:**
```bash
export TMDB_API_KEY=your-api-key
export JWT_SECRET=your-secret-key
export DATABASE_URL=jdbc:postgresql://localhost:5432/filmreview_dev
./gradlew bootRun
```

**On Windows (PowerShell):**
```powershell
$env:TMDB_API_KEY="your-api-key"
$env:JWT_SECRET="your-secret-key"
./gradlew bootRun
```

**Using a .env file with a script:**

Create `backend/.env`:
```
TMDB_API_KEY=your-api-key
JWT_SECRET=your-secret-key
DATABASE_URL=jdbc:postgresql://localhost:5432/filmreview_dev
```

Then load it before running (macOS/Linux):
```bash
export $(cat .env | xargs)
./gradlew bootRun
```

Or use a simple script `backend/run.sh`:
```bash
#!/bin/bash
export $(cat .env | xargs)
./gradlew bootRun
```

## Option 3: Set in VS Code launch.json

Update `.vscode/launch.json` to include environment variables:

```json
{
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.filmreview.FilmReviewBackendApplication",
      "projectName": "backend",
      "vmArgs": "-Dspring.profiles.active=local",
      "env": {
        "TMDB_API_KEY": "your-api-key-here",
        "JWT_SECRET": "your-secret-here"
      }
    }
  ]
}
```

## Option 4: Use System Properties

Set via command line:
```bash
./gradlew bootRun --args='--tmdb.api-key=your-key --jwt.secret=your-secret'
```

## Configuration Priority

Spring Boot reads configuration in this order (highest to lowest priority):
1. System properties (`-Dproperty=value`)
2. Environment variables
3. `application-{profile}.yml` (e.g., `application-dev.yml`)
4. `application.yml` (with defaults)

**Note:** `application-dev.yml` is gitignored, so you can safely store your local API keys and secrets there.

## Available Environment Variables

- `TMDB_API_KEY` - TMDB API key
- `TMDB_IMAGE_BASE_URL` - TMDB image base URL (default: https://image.tmdb.org/t/p)
- `JWT_SECRET` - JWT signing secret
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_PASSWORD` - Redis password (optional)
- `PORT` - Server port (default: 8080)
