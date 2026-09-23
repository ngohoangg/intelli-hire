# Intelli Hire Question Service

A Spring Boot microservice for managing questions in the Intelli Hire platform.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **PostgreSQL** - Primary database
- **Redis** - Caching
- **Flyway** - Database migration
- **MapStruct** - Object mapping
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Redis 7+

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd intelli-hire-question
```

### 2. Configure database

Create a PostgreSQL database:

```sql
CREATE DATABASE intelli_hire_question;
```

### 3. Configure environment variables

Set the following environment variables or update `application.yml`:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/intelli_hire_question
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 4. Run the application

```bash
# Using Maven
./mvnw spring-boot:run

# Or build and run
./mvnw clean package
java -jar target/intelli-hire-question-0.0.1-SNAPSHOT.jar
```

## API Documentation

Once the application is running, access the API documentation at:

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs

## Project Structure

```
src/
├── main/
│   ├── java/org/aibles/intellihirequestion/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handling
│   │   ├── repository/      # Data repositories
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.yml  # Application config
└── test/
    ├── java/                # Test classes
    └── resources/           # Test resources
```

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Building

```bash
# Build without tests
./mvnw clean package -DskipTests

# Build with tests
./mvnw clean package
```

## Docker (Optional)

```bash
# Build Docker image
docker build -t intelli-hire-question .

# Run with Docker Compose
docker-compose up -d
```

## Health Check

- Health endpoint: http://localhost:8080/api/health
- Actuator health: http://localhost:8080/api/actuator/health

## License

Private - All Rights Reserved
