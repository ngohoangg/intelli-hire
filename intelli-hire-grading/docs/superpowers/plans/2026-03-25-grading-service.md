# IntelliHire Grading Service Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Grading Service that receives batch interview answers via REST API, grades them in parallel using OpenRouter LLM, stores results, and auto-creates reports via Report Service.

**Architecture:** Layered Spring Boot microservice (Controller → Service → Repository → Entity) following exact patterns from intelli-hire-report. Parallel LLM calls via CompletableFuture with a configurable thread pool. WebClient for OpenRouter and Report Service integration.

**Tech Stack:** Java 17, Spring Boot 3.2.1, PostgreSQL 16, Redis 7, Flyway, WebClient (webflux), Lombok, springdoc-openapi

**Spec:** `docs/superpowers/specs/2026-03-25-grading-service-design.md`

**Reference:** `C:\Users\ACER\Desktop\aibless\intelli-hire-report\` (copy patterns from this service)

---

## File Map

| File | Responsibility |
|------|---------------|
| `pom.xml` | Maven dependencies (Spring Boot 3.2.1, webflux, JPA, Redis, Flyway, PostgreSQL, Lombok, springdoc) |
| `Dockerfile` | Multi-stage build (maven → JRE) |
| `docker-compose.yml` | PostgreSQL + Redis for local dev |
| `.env` | Environment variables |
| `src/main/resources/application.yml` | All config (DB, Redis, OpenRouter, thread pool, server) |
| `src/main/resources/db/migration/V1__init.sql` | Schema: grading_sessions, grading_results, indexes |
| `IntelliHireGradingApplication.java` | Main class |
| `config/JpaAuditingConfig.java` | JPA auditing |
| `config/RedisConfig.java` | Cache manager with TTLs |
| `config/OpenApiConfig.java` | Swagger config |
| `config/OpenRouterConfig.java` | WebClient bean + properties for OpenRouter |
| `config/AsyncConfig.java` | Thread pool executor for parallel LLM calls |
| `entity/BaseEntity.java` | UUID ID, audit fields |
| `entity/enums/GradingStatus.java` | PROCESSING, COMPLETED, FAILED |
| `entity/GradingSession.java` | Session entity |
| `entity/GradingResult.java` | Per-question result entity |
| `dto/BaseResponse.java` | Standard API wrapper |
| `dto/FieldError.java` | Validation error detail |
| `dto/PageResponse.java` | Pagination wrapper |
| `dto/GradeRequest.java` | Batch grading input |
| `dto/AnswerRequest.java` | Per-question input |
| `dto/GradingResponse.java` | Full grading output |
| `dto/GradingResultResponse.java` | Per-question output |
| `dto/GradingSummaryResponse.java` | Lightweight list item |
| `dto/openrouter/ChatRequest.java` | OpenRouter API request |
| `dto/openrouter/ChatResponse.java` | OpenRouter API response |
| `dto/openrouter/LlmGradingResult.java` | Parsed LLM score + feedback |
| `exception/BaseException.java` | Abstract exception |
| `exception/ErrorCode.java` | GRD_001..008, COM_001..003 |
| `exception/NotFoundException.java` | 404 |
| `exception/BadRequestException.java` | 400 |
| `exception/DuplicateException.java` | 409 |
| `exception/ForbiddenException.java` | 403 |
| `exception/GlobalExceptionHandler.java` | @RestControllerAdvice |
| `mapper/GradingMapper.java` | Entity ↔ DTO mapping |
| `repository/GradingSessionRepository.java` | JPA queries for sessions |
| `repository/GradingResultRepository.java` | JPA queries for results |
| `service/GradingService.java` | Interface |
| `service/LlmService.java` | Interface |
| `service/ReportClientService.java` | Interface |
| `service/impl/GradingServiceImpl.java` | Core grading logic + parallel calls |
| `service/impl/OpenRouterLlmServiceImpl.java` | OpenRouter WebClient integration |
| `service/impl/ReportClientServiceImpl.java` | Report Service REST client |
| `utils/PaginationUtils.java` | Pagination validation |
| `utils/SecurityUtils.java` | X-User-Name header extraction |
| `utils/PromptBuilder.java` | LLM prompt construction |
| `controller/GradingController.java` | REST endpoints |

**Test files:**
| File | Tests |
|------|-------|
| `src/test/resources/application-test.yml` | H2 test config |
| `src/test/java/.../controller/GradingControllerIntegrationTest.java` | Full API tests |
| `src/test/java/.../service/GradingServiceImplTest.java` | Unit tests with mocked LLM |
| `src/test/java/.../service/OpenRouterLlmServiceImplTest.java` | LLM response parsing tests |
| `src/test/java/.../utils/PromptBuilderTest.java` | Prompt construction tests |

---

## Task 1: Project Scaffold & Build Config

**Files:**
- Create: `pom.xml`
- Create: `Dockerfile`
- Create: `docker-compose.yml`
- Create: `.env`
- Create: `src/main/java/org/aibles/intellihiregrading/IntelliHireGradingApplication.java`

- [ ] **Step 1: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>

    <groupId>org.aibles</groupId>
    <artifactId>intelli-hire-grading</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>intelli-hire-grading</name>
    <description>Intelli Hire Grading Service</description>

    <properties>
        <java.version>17</java.version>
        <lombok.version>1.18.34</lombok.version>
        <springdoc.version>2.3.0</springdoc.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- WebClient for OpenRouter & Report Service -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>10.10.0</version>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- OpenAPI/Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>mockwebserver</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <fork>true</fork>
                    <compilerArgs>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create Dockerfile**

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 3: Create docker-compose.yml**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: intelli-hire-grading-postgres
    environment:
      POSTGRES_DB: ${DATABASE_NAME}
      POSTGRES_USER: ${DATABASE_USERNAME}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    ports:
      - "${DB_PORT}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: intelli-hire-grading-redis
    ports:
      - "${REDIS_PORT}:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

- [ ] **Step 4: Create .env**

```properties
# Database PostgreSQL
DATABASE_URL=jdbc:postgresql://localhost:5433/intelli_hire_grading
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=Trang@051203
DB_HOST=localhost
DB_PORT=5433
DATABASE_NAME=intelli_hire_grading

# Flyway
FLYWAY_ENABLED=true

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380
REDIS_PASSWORD=

# Server
SERVER_PORT=8084
SPRING_PROFILES_ACTIVE=local

# OpenRouter
OPENROUTER_API_KEY=sk-or-your-key-here
OPENROUTER_MODEL=openai/gpt-4o
OPENROUTER_TEMPERATURE=0.3
OPENROUTER_MAX_TOKENS=1024
OPENROUTER_TIMEOUT=30000

# Report Service
REPORT_SERVICE_URL=http://localhost:8085/api
```

- [ ] **Step 5: Create main application class**

Create `src/main/java/org/aibles/intellihiregrading/IntelliHireGradingApplication.java`:

```java
package org.aibles.intellihiregrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntelliHireGradingApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliHireGradingApplication.class, args);
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add pom.xml Dockerfile docker-compose.yml .env src/main/java/org/aibles/intellihiregrading/IntelliHireGradingApplication.java
git commit -m "feat(grading): scaffold project with Maven, Docker, and main class"
```

---

## Task 2: Configuration & Database Migration

**Files:**
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/db/migration/V1__init.sql`
- Create: `src/test/resources/application-test.yml`

- [ ] **Step 1: Create application.yml**

```yaml
spring:
  application:
    name: intelli-hire-grading

  profiles:
    active: local

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5433/intelli_hire_grading}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:Trang@051203}
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      pool-name: IntelliHireGradingHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6380}
      password: ${REDIS_PASSWORD:}
      timeout: 60000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

server:
  port: ${SERVER_PORT:8084}
  servlet:
    context-path: ${CONTEXT_PATH:/api}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

logging:
  level:
    root: INFO
    org.aibles: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG

# OpenRouter LLM config
openrouter:
  api-key: ${OPENROUTER_API_KEY:}
  base-url: ${OPENROUTER_BASE_URL:https://openrouter.ai/api/v1}
  model: ${OPENROUTER_MODEL:openai/gpt-4o}
  temperature: ${OPENROUTER_TEMPERATURE:0.3}
  max-tokens: ${OPENROUTER_MAX_TOKENS:1024}
  timeout: ${OPENROUTER_TIMEOUT:30000}

# Report Service client
report-service:
  base-url: ${REPORT_SERVICE_URL:http://localhost:8085/api}

# Async thread pool for parallel LLM calls
grading:
  thread-pool:
    core-size: ${GRADING_POOL_CORE:10}
    max-size: ${GRADING_POOL_MAX:20}
    queue-capacity: ${GRADING_POOL_QUEUE:100}
```

- [ ] **Step 2: Create V1__init.sql**

```sql
-- Grading Sessions table
CREATE TABLE IF NOT EXISTS grading_sessions (
    id              VARCHAR(36) PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL UNIQUE,
    user_id         BIGINT NOT NULL,
    topic           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    total_questions INTEGER DEFAULT 0,
    graded_count    INTEGER DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT NOW(),
    created_by      VARCHAR(255),
    last_updated_at TIMESTAMP DEFAULT NOW(),
    last_updated_by VARCHAR(255)
);

-- Grading Results table
CREATE TABLE IF NOT EXISTS grading_results (
    id                 VARCHAR(36) PRIMARY KEY,
    grading_session_id VARCHAR(36) NOT NULL REFERENCES grading_sessions(id) ON DELETE CASCADE,
    question_id        VARCHAR(36) NOT NULL,
    question_text      TEXT,
    answer_text        TEXT,
    skill_name         VARCHAR(255),
    score              DOUBLE PRECISION DEFAULT 0,
    max_score          DOUBLE PRECISION DEFAULT 10,
    feedback           TEXT,
    prompt_used        TEXT,
    model_used         VARCHAR(100),
    latency_ms         BIGINT,
    created_at         TIMESTAMP DEFAULT NOW(),
    created_by         VARCHAR(255),
    last_updated_at    TIMESTAMP DEFAULT NOW(),
    last_updated_by    VARCHAR(255)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_grading_sessions_session_id ON grading_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_user_id ON grading_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_grading_sessions_status ON grading_sessions(status);
CREATE INDEX IF NOT EXISTS idx_grading_results_session_id ON grading_results(grading_session_id);
CREATE INDEX IF NOT EXISTS idx_grading_results_question_id ON grading_results(question_id);
```

- [ ] **Step 3: Create application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false
  cache:
    type: none
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 0

logging:
  level:
    root: WARN
    org.aibles: DEBUG

openrouter:
  api-key: test-key
  base-url: http://localhost:8888
  model: test-model
  temperature: 0.3
  max-tokens: 1024
  timeout: 5000

report-service:
  base-url: http://localhost:9999

grading:
  thread-pool:
    core-size: 2
    max-size: 4
    queue-capacity: 10
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/ src/test/resources/
git commit -m "feat(grading): add application config, Flyway migration, and test config"
```

---

## Task 3: Entity Layer

**Files:**
- Create: `entity/BaseEntity.java`
- Create: `entity/enums/GradingStatus.java`
- Create: `entity/GradingSession.java`
- Create: `entity/GradingResult.java`

All files under `src/main/java/org/aibles/intellihiregrading/`

- [ ] **Step 1: Create BaseEntity.java** (copy from Report Service)

```java
package org.aibles.intellihiregrading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @LastModifiedBy
    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @PrePersist
    public void initId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
```

- [ ] **Step 2: Create GradingStatus.java**

```java
package org.aibles.intellihiregrading.entity.enums;

public enum GradingStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}
```

- [ ] **Step 3: Create GradingSession.java**

```java
package org.aibles.intellihiregrading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grading_sessions")
public class GradingSession extends BaseEntity {

    @Column(name = "session_id", nullable = false, unique = true, updatable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "topic")
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GradingStatus status = GradingStatus.PROCESSING;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "graded_count")
    @Builder.Default
    private Integer gradedCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
```

- [ ] **Step 4: Create GradingResult.java**

```java
package org.aibles.intellihiregrading.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grading_results")
public class GradingResult extends BaseEntity {

    @Column(name = "grading_session_id", nullable = false)
    private String gradingSessionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "score")
    @Builder.Default
    private Double score = 0.0;

    @Column(name = "max_score")
    @Builder.Default
    private Double maxScore = 10.0;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "latency_ms")
    private Long latencyMs;
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/entity/
git commit -m "feat(grading): add entity layer - BaseEntity, GradingSession, GradingResult"
```

---

## Task 4: Exception & Error Handling

**Files:**
- Create: `exception/ErrorCode.java`
- Create: `exception/BaseException.java`
- Create: `exception/NotFoundException.java`
- Create: `exception/BadRequestException.java`
- Create: `exception/DuplicateException.java`
- Create: `exception/ForbiddenException.java`
- Create: `exception/GlobalExceptionHandler.java`

All under `src/main/java/org/aibles/intellihiregrading/exception/`

- [ ] **Step 1: Create ErrorCode.java**

```java
package org.aibles.intellihiregrading.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Grading errors
    GRD_001("GRD_001", "Grading session not found"),
    GRD_002("GRD_002", "Grading already exists for session"),
    GRD_003("GRD_003", "Invalid session id"),
    GRD_004("GRD_004", "Invalid grading status"),
    GRD_005("GRD_005", "Access denied, admin role required"),
    GRD_006("GRD_006", "LLM service unavailable"),
    GRD_007("GRD_007", "LLM response parse error"),
    GRD_008("GRD_008", "Report service unavailable"),

    // Common errors
    COM_001("COM_001", "Validation error"),
    COM_002("COM_002", "Internal server error"),
    COM_003("COM_003", "Bad request");

    private final String code;
    private final String message;
}
```

- [ ] **Step 2: Create BaseException.java**

```java
package org.aibles.intellihiregrading.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    protected BaseException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.status = status;
    }

    protected BaseException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
```

- [ ] **Step 3: Create concrete exception classes**

**NotFoundException.java:**
```java
package org.aibles.intellihiregrading.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }
}
```

**BadRequestException.java:**
```java
package org.aibles.intellihiregrading.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }
}
```

**DuplicateException.java:**
```java
package org.aibles.intellihiregrading.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends BaseException {
    public DuplicateException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.CONFLICT);
    }
}
```

**ForbiddenException.java:**
```java
package org.aibles.intellihiregrading.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.FORBIDDEN);
    }
}
```

- [ ] **Step 4: Create GlobalExceptionHandler.java** (copy pattern from Report)

```java
package org.aibles.intellihiregrading.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.BaseResponse;
import org.aibles.intellihiregrading.dto.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
        log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(BaseResponse.error(ex.getErrorCode(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_001.getCode(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());
        List<FieldError> errors = ex.getConstraintViolations().stream()
                .map(violation -> FieldError.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_001.getCode(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ErrorCode.COM_003.getCode(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected exception: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(ErrorCode.COM_002.getCode(), null));
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/exception/
git commit -m "feat(grading): add exception hierarchy and global error handler"
```

---

## Task 5: DTOs

**Files:**
- Create: `dto/BaseResponse.java`
- Create: `dto/FieldError.java`
- Create: `dto/PageResponse.java`
- Create: `dto/GradeRequest.java`
- Create: `dto/AnswerRequest.java`
- Create: `dto/GradingResponse.java`
- Create: `dto/GradingResultResponse.java`
- Create: `dto/GradingSummaryResponse.java`
- Create: `dto/openrouter/ChatRequest.java`
- Create: `dto/openrouter/ChatResponse.java`
- Create: `dto/openrouter/LlmGradingResult.java`

All under `src/main/java/org/aibles/intellihiregrading/dto/`

- [ ] **Step 1: Create shared DTOs** (BaseResponse, FieldError, PageResponse — copy from Report)

**BaseResponse.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BaseResponse<T> {
    private boolean success;
    private String errorCode;
    private T data;
    private List<FieldError> errors;
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true).data(data).timestamp(LocalDateTime.now()).build();
    }

    public static <T> BaseResponse<T> error(String errorCode, List<FieldError> errors) {
        return BaseResponse.<T>builder()
                .success(false).errorCode(errorCode).errors(errors).timestamp(LocalDateTime.now()).build();
    }
}
```

**FieldError.java:**
```java
package org.aibles.intellihiregrading.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;
}
```

**PageResponse.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent()).page(page.getNumber()).size(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .isFirst(page.isFirst()).isLast(page.isLast()).build();
    }
}
```

- [ ] **Step 2: Create request DTOs**

**GradeRequest.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradeRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String topic;

    @NotEmpty(message = "Answers must not be empty")
    @Valid
    private List<AnswerRequest> answers;
}
```

**AnswerRequest.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnswerRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    private String questionText;

    private String answerText;

    private String skillName;

    @Min(value = 1, message = "Max score must be >= 1")
    @Max(value = 100, message = "Max score must be <= 100")
    @Builder.Default
    private Double maxScore = 10.0;
}
```

- [ ] **Step 3: Create response DTOs**

**GradingResponse.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradingResponse {
    private String id;
    private String sessionId;
    private Long userId;
    private String topic;
    private String status;
    private Integer totalQuestions;
    private Double totalScore;
    private Double maxScore;
    private List<GradingResultResponse> results;
    private LocalDateTime createdAt;
}
```

**GradingResultResponse.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradingResultResponse {
    private String id;
    private String questionId;
    private String questionText;
    private String answerText;
    private String skillName;
    private Double score;
    private Double maxScore;
    private String feedback;
    private String modelUsed;
    private Long latencyMs;
    private LocalDateTime createdAt;
}
```

**GradingSummaryResponse.java:**
```java
package org.aibles.intellihiregrading.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GradingSummaryResponse {
    private String id;
    private String sessionId;
    private Long userId;
    private String topic;
    private String status;
    private Integer totalQuestions;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: Create OpenRouter DTOs**

**ChatRequest.java:**
```java
package org.aibles.intellihiregrading.dto.openrouter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
```

**ChatResponse.java:**
```java
package org.aibles.intellihiregrading.dto.openrouter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatResponse {
    private List<Choice> choices;
    private Usage usage;
    private String model;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Message message;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
```

**LlmGradingResult.java:**
```java
package org.aibles.intellihiregrading.dto.openrouter;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmGradingResult {
    private Double score;
    private String feedback;
    private String model;
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/dto/
git commit -m "feat(grading): add all DTOs - request, response, OpenRouter, shared"
```

---

## Task 6: Config Classes

**Files:**
- Create: `config/JpaAuditingConfig.java`
- Create: `config/RedisConfig.java`
- Create: `config/OpenApiConfig.java`
- Create: `config/OpenRouterConfig.java`
- Create: `config/AsyncConfig.java`
- Create: `utils/SecurityUtils.java`

All under `src/main/java/org/aibles/intellihiregrading/`

- [ ] **Step 1: Create SecurityUtils.java** (needed by JpaAuditingConfig)

```java
package org.aibles.intellihiregrading.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {
    public static String getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String userFromHeader = attributes.getRequest().getHeader("X-User-Name");
            return (userFromHeader != null) ? userFromHeader : "SYSTEM";
        }
        return "SYSTEM";
    }
}
```

- [ ] **Step 2: Create JpaAuditingConfig.java**

```java
package org.aibles.intellihiregrading.config;

import org.aibles.intellihiregrading.utils.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(SecurityUtils.getCurrentUser());
    }
}
```

- [ ] **Step 3: Create RedisConfig.java**

```java
package org.aibles.intellihiregrading.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class RedisConfig implements CachingConfigurer {

    private GenericJackson2JsonRedisSerializer buildSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = buildSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = buildSerializer();
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("grading", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("grading_session", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
```

- [ ] **Step 4: Create OpenApiConfig.java**

```java
package org.aibles.intellihiregrading.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Intelli Hire Grading API")
                        .version("1.0.0")
                        .description("AI-powered interview answer grading service")
                        .contact(new Contact().name("Aibles").email("contact@aibles.com")))
                .servers(List.of(new Server().url("http://localhost:8084/api")));
    }
}
```

- [ ] **Step 5: Create OpenRouterConfig.java**

```java
package org.aibles.intellihiregrading.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterConfig {

    private String apiKey;
    private String baseUrl;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Integer timeout;

    @Bean(name = "openRouterWebClient")
    public WebClient openRouterWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
}
```

- [ ] **Step 6: Create AsyncConfig.java**

```java
package org.aibles.intellihiregrading.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Getter
@Setter
@Configuration
@EnableAsync
@ConfigurationProperties(prefix = "grading.thread-pool")
public class AsyncConfig {

    private int coreSize = 10;
    private int maxSize = 20;
    private int queueCapacity = 100;

    @Bean(name = "gradingTaskExecutor")
    public Executor gradingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("grading-llm-");
        executor.initialize();
        return executor;
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/config/ src/main/java/org/aibles/intellihiregrading/utils/SecurityUtils.java
git commit -m "feat(grading): add config classes - JPA, Redis, OpenAPI, OpenRouter, Async"
```

---

## Task 7: Repository & Mapper & Utils

**Files:**
- Create: `repository/GradingSessionRepository.java`
- Create: `repository/GradingResultRepository.java`
- Create: `mapper/GradingMapper.java`
- Create: `utils/PaginationUtils.java`
- Create: `utils/PromptBuilder.java`

- [ ] **Step 1: Create GradingSessionRepository.java**

```java
package org.aibles.intellihiregrading.repository;

import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradingSessionRepository extends JpaRepository<GradingSession, String> {

    Optional<GradingSession> findBySessionId(String sessionId);

    boolean existsBySessionIdAndStatusNot(String sessionId, GradingStatus status);

    Optional<GradingSession> findBySessionIdAndStatus(String sessionId, GradingStatus status);

    Page<GradingSession> findByUserId(Long userId, Pageable pageable);

    Page<GradingSession> findByUserIdAndStatus(Long userId, GradingStatus status, Pageable pageable);
}
```

- [ ] **Step 2: Create GradingResultRepository.java**

```java
package org.aibles.intellihiregrading.repository;

import org.aibles.intellihiregrading.entity.GradingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradingResultRepository extends JpaRepository<GradingResult, String> {

    List<GradingResult> findByGradingSessionId(String gradingSessionId);

    @Modifying
    @Transactional
    void deleteByGradingSessionId(String gradingSessionId);
}
```

- [ ] **Step 3: Create GradingMapper.java**

```java
package org.aibles.intellihiregrading.mapper;

import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradingMapper {

    public GradingSession toEntity(GradeRequest request) {
        if (request == null) return null;
        return GradingSession.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .topic(request.getTopic())
                .totalQuestions(request.getAnswers().size())
                .build();
    }

    public GradingResponse toGradingResponse(GradingSession session, List<GradingResult> results) {
        if (session == null) return null;

        double totalScore = results.stream()
                .mapToDouble(r -> r.getScore() != null ? r.getScore() : 0.0).sum();
        double maxScore = results.stream()
                .mapToDouble(r -> r.getMaxScore() != null ? r.getMaxScore() : 0.0).sum();

        return GradingResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .topic(session.getTopic())
                .status(session.getStatus().name())
                .totalQuestions(session.getTotalQuestions())
                .totalScore(totalScore)
                .maxScore(maxScore)
                .results(results.stream().map(this::toResultResponse).collect(Collectors.toList()))
                .createdAt(session.getCreatedAt())
                .build();
    }

    public GradingResultResponse toResultResponse(GradingResult result) {
        if (result == null) return null;
        return GradingResultResponse.builder()
                .id(result.getId())
                .questionId(result.getQuestionId())
                .questionText(result.getQuestionText())
                .answerText(result.getAnswerText())
                .skillName(result.getSkillName())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .feedback(result.getFeedback())
                .modelUsed(result.getModelUsed())
                .latencyMs(result.getLatencyMs())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public GradingSummaryResponse toSummaryResponse(GradingSession session) {
        if (session == null) return null;
        return GradingSummaryResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .topic(session.getTopic())
                .status(session.getStatus().name())
                .totalQuestions(session.getTotalQuestions())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 4: Create PaginationUtils.java**

```java
package org.aibles.intellihiregrading.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PaginationUtils {

    public static final Set<String> GRADING_SORTABLE_FIELDS = Set.of(
            "createdAt", "lastUpdatedAt"
    );

    private PaginationUtils() {}

    public static Pageable buildPageable(int page, int size, String sort, Set<String> sortableFields) {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        if (!sortableFields.contains(sortField)) {
            sortField = "createdAt";
        }

        Sort.Direction direction;
        try {
            direction = sortParams.length > 1
                    ? Sort.Direction.fromString(sortParams[1])
                    : Sort.Direction.DESC;
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
```

- [ ] **Step 5: Create PromptBuilder.java**

```java
package org.aibles.intellihiregrading.utils;

import org.aibles.intellihiregrading.dto.AnswerRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    private static final String GRADING_PROMPT_TEMPLATE = """
            You are a technical interviewer evaluating a candidate's answer.
            Grade the answer strictly based on technical accuracy, completeness, and clarity.

            Topic: %s
            Skill: %s
            Question: %s
            Candidate's Answer: %s
            Max Score: %.1f

            Respond ONLY with a valid JSON object (no markdown, no extra text):
            {"score": <number between 0 and %.1f>, "feedback": "<detailed feedback explaining the score>"}
            """;

    public String build(AnswerRequest answer, String topic) {
        return String.format(
                GRADING_PROMPT_TEMPLATE,
                topic != null ? topic : "General",
                answer.getSkillName() != null ? answer.getSkillName() : "General",
                answer.getQuestionText() != null ? answer.getQuestionText() : "",
                answer.getAnswerText() != null ? answer.getAnswerText() : "(no answer provided)",
                answer.getMaxScore(),
                answer.getMaxScore()
        );
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/repository/ src/main/java/org/aibles/intellihiregrading/mapper/ src/main/java/org/aibles/intellihiregrading/utils/
git commit -m "feat(grading): add repositories, mapper, pagination utils, and prompt builder"
```

---

## Task 8: LLM Service (OpenRouter Integration)

**Files:**
- Create: `service/LlmService.java`
- Create: `service/impl/OpenRouterLlmServiceImpl.java`
- Create: `src/test/java/org/aibles/intellihiregrading/service/OpenRouterLlmServiceImplTest.java`

- [ ] **Step 1: Create LlmService interface**

```java
package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;

public interface LlmService {
    LlmGradingResult grade(String prompt);
}
```

- [ ] **Step 2: Create OpenRouterLlmServiceImpl**

```java
package org.aibles.intellihiregrading.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.config.OpenRouterConfig;
import org.aibles.intellihiregrading.dto.openrouter.ChatRequest;
import org.aibles.intellihiregrading.dto.openrouter.ChatResponse;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.service.LlmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterLlmServiceImpl implements LlmService {

    @Qualifier("openRouterWebClient")
    private final WebClient openRouterWebClient;
    private final OpenRouterConfig openRouterConfig;
    private final ObjectMapper objectMapper;

    @Override
    public LlmGradingResult grade(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .model(openRouterConfig.getModel())
                .temperature(openRouterConfig.getTemperature())
                .maxTokens(openRouterConfig.getMaxTokens())
                .messages(List.of(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .build();

        try {
            ChatResponse response = callWithRetry(request);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("LLM grading failed: {}", e.getMessage(), e);
            return LlmGradingResult.builder()
                    .score(0.0)
                    .feedback("Unable to grade: " + e.getMessage())
                    .model(openRouterConfig.getModel())
                    .build();
        }
    }

    private ChatResponse callWithRetry(ChatRequest request) {
        try {
            return callOpenRouter(request);
        } catch (Exception e) {
            if (isRetryable(e)) {
                log.warn("LLM call failed, retrying in 2s: {}", e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                return callOpenRouter(request);
            }
            throw e;
        }
    }

    private boolean isRetryable(Throwable e) {
        if (e instanceof WebClientResponseException wce) {
            int status = wce.getStatusCode().value();
            return status == 500 || status == 502 || status == 503 || status == 504;
        }
        return e instanceof java.util.concurrent.TimeoutException
                || e.getCause() instanceof java.util.concurrent.TimeoutException;
    }

    private ChatResponse callOpenRouter(ChatRequest request) {
        return openRouterWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .timeout(Duration.ofMillis(openRouterConfig.getTimeout()))
                .block();
    }

    private LlmGradingResult parseResponse(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return fallbackResult("Empty LLM response");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        String model = response.getModel() != null ? response.getModel() : openRouterConfig.getModel();

        // Strip markdown code fences if present
        content = stripMarkdownFences(content).trim();

        try {
            JsonNode node = objectMapper.readTree(content);
            double score = node.has("score") ? node.get("score").asDouble() : 0.0;
            String feedback = node.has("feedback") ? node.get("feedback").asText() : "No feedback provided";
            return LlmGradingResult.builder()
                    .score(score)
                    .feedback(feedback)
                    .model(model)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse LLM response as JSON: {}", content, e);
            return LlmGradingResult.builder()
                    .score(0.0)
                    .feedback("Parse error. Raw response: " + content)
                    .model(model)
                    .build();
        }
    }

    private String stripMarkdownFences(String content) {
        if (content == null) return "";
        // Match ```json ... ``` or ``` ... ``` wrapping the entire content
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?s)^\\s*```(?:json)?\\s*(.*?)\\s*```\\s*$")
                .matcher(content);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return content.trim();
    }

    private LlmGradingResult fallbackResult(String reason) {
        return LlmGradingResult.builder()
                .score(0.0)
                .feedback("Unable to grade: " + reason)
                .model(openRouterConfig.getModel())
                .build();
    }
}
```

- [ ] **Step 3: Write unit test for LLM response parsing**

Create `src/test/java/org/aibles/intellihiregrading/service/OpenRouterLlmServiceImplTest.java`:

```java
package org.aibles.intellihiregrading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.aibles.intellihiregrading.config.OpenRouterConfig;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.service.impl.OpenRouterLlmServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class OpenRouterLlmServiceImplTest {

    private MockWebServer mockServer;
    private OpenRouterLlmServiceImpl llmService;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();

        OpenRouterConfig config = new OpenRouterConfig();
        config.setApiKey("test-key");
        config.setBaseUrl(mockServer.url("/").toString());
        config.setModel("test-model");
        config.setTemperature(0.3);
        config.setMaxTokens(1024);
        config.setTimeout(5000);

        WebClient webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();

        llmService = new OpenRouterLlmServiceImpl(webClient, config, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    @Test
    void grade_validJsonResponse_parsesCorrectly() {
        String body = """
                {"choices":[{"message":{"role":"assistant","content":"{\\"score\\": 8.5, \\"feedback\\": \\"Good answer\\"}"}}],"model":"test-model"}
                """;
        mockServer.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(8.5, result.getScore());
        assertEquals("Good answer", result.getFeedback());
    }

    @Test
    void grade_markdownWrappedResponse_stripsAndParses() {
        String body = """
                {"choices":[{"message":{"role":"assistant","content":"```json\\n{\\"score\\": 7.0, \\"feedback\\": \\"Decent\\"}\\n```"}}],"model":"test-model"}
                """;
        mockServer.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(7.0, result.getScore());
    }

    @Test
    void grade_invalidJson_returnsFallback() {
        String body = """
                {"choices":[{"message":{"role":"assistant","content":"This is not JSON at all"}}],"model":"test-model"}
                """;
        mockServer.enqueue(new MockResponse().setBody(body).setHeader("Content-Type", "application/json"));

        LlmGradingResult result = llmService.grade("test prompt");

        assertEquals(0.0, result.getScore());
        assertTrue(result.getFeedback().contains("Parse error"));
    }
}
```

- [ ] **Step 4: Run tests**

Run: `cd C:\Users\ACER\Desktop\aibless\intelli-hire-grading && mvn test -pl . -Dtest=OpenRouterLlmServiceImplTest -Dspring.profiles.active=test`
Expected: All 3 tests PASS (mockwebserver already in pom.xml from Task 1)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/service/LlmService.java src/main/java/org/aibles/intellihiregrading/service/impl/OpenRouterLlmServiceImpl.java src/test/ pom.xml
git commit -m "feat(grading): add OpenRouter LLM service with retry, markdown stripping, and tests"
```

---

## Task 9: Report Client Service

**Files:**
- Create: `service/ReportClientService.java`
- Create: `service/impl/ReportClientServiceImpl.java`

- [ ] **Step 1: Create ReportClientService interface**

```java
package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;

import java.util.List;

public interface ReportClientService {
    void createReport(GradingSession session, List<GradingResult> results);
}
```

- [ ] **Step 2: Create ReportClientServiceImpl**

```java
package org.aibles.intellihiregrading.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.service.ReportClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportClientServiceImpl implements ReportClientService {

    private final WebClient reportWebClient;

    public ReportClientServiceImpl(@Value("${report-service.base-url}") String reportServiceUrl) {
        this.reportWebClient = WebClient.builder()
                .baseUrl(reportServiceUrl)
                .build();
    }

    @Override
    public void createReport(GradingSession session, List<GradingResult> results) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("session_id", session.getSessionId());
            request.put("user_id", session.getUserId());
            request.put("topic", session.getTopic());
            request.put("summary", null);
            request.put("roadmap_json", null);
            request.put("question_scores", results.stream()
                    .map(this::toQuestionScoreMap)
                    .collect(Collectors.toList()));

            reportWebClient.post()
                    .uri("/v1/reports")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("Report created for sessionId: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to create report for sessionId: {}. Error: {}",
                    session.getSessionId(), e.getMessage(), e);
            // Do NOT throw — grading result still returned to client
        }
    }

    private Map<String, Object> toQuestionScoreMap(GradingResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("question_id", result.getQuestionId());
        map.put("question_text", result.getQuestionText());
        map.put("answer_text", result.getAnswerText());
        map.put("score", result.getScore());
        map.put("max_score", result.getMaxScore());
        map.put("feedback", result.getFeedback());
        map.put("skill_name", result.getSkillName());
        return map;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/service/ReportClientService.java src/main/java/org/aibles/intellihiregrading/service/impl/ReportClientServiceImpl.java
git commit -m "feat(grading): add Report Service REST client with error tolerance"
```

---

## Task 10: Core Grading Service

**Files:**
- Create: `service/GradingService.java`
- Create: `service/impl/GradingServiceImpl.java`

- [ ] **Step 1: Create GradingService interface**

```java
package org.aibles.intellihiregrading.service;

import org.aibles.intellihiregrading.dto.*;
import org.springframework.data.domain.Pageable;

public interface GradingService {

    GradingResponse grade(GradeRequest request);

    GradingResponse getById(String id);

    GradingResponse getBySessionId(String sessionId);

    PageResponse<GradingSummaryResponse> getHistoryByUserId(Long userId, String status, Pageable pageable);

    PageResponse<GradingSummaryResponse> getAllGradings(Pageable pageable);
}
```

- [ ] **Step 2: Create GradingServiceImpl**

```java
package org.aibles.intellihiregrading.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.entity.GradingResult;
import org.aibles.intellihiregrading.entity.GradingSession;
import org.aibles.intellihiregrading.entity.enums.GradingStatus;
import org.aibles.intellihiregrading.exception.*;
import org.aibles.intellihiregrading.mapper.GradingMapper;
import org.aibles.intellihiregrading.repository.GradingResultRepository;
import org.aibles.intellihiregrading.repository.GradingSessionRepository;
import org.aibles.intellihiregrading.service.GradingService;
import org.aibles.intellihiregrading.service.LlmService;
import org.aibles.intellihiregrading.service.ReportClientService;
import org.aibles.intellihiregrading.utils.PromptBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GradingServiceImpl implements GradingService {

    private final GradingSessionRepository sessionRepository;
    private final GradingResultRepository resultRepository;
    private final LlmService llmService;
    private final ReportClientService reportClientService;
    private final GradingMapper gradingMapper;
    private final PromptBuilder promptBuilder;
    private final Executor gradingTaskExecutor;
    private final TransactionTemplate transactionTemplate;

    public GradingServiceImpl(
            GradingSessionRepository sessionRepository,
            GradingResultRepository resultRepository,
            LlmService llmService,
            ReportClientService reportClientService,
            GradingMapper gradingMapper,
            PromptBuilder promptBuilder,
            @Qualifier("gradingTaskExecutor") Executor gradingTaskExecutor,
            TransactionTemplate transactionTemplate) {
        this.sessionRepository = sessionRepository;
        this.resultRepository = resultRepository;
        this.llmService = llmService;
        this.reportClientService = reportClientService;
        this.gradingMapper = gradingMapper;
        this.promptBuilder = promptBuilder;
        this.gradingTaskExecutor = gradingTaskExecutor;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public GradingResponse grade(GradeRequest request) {
        log.info("Starting grading for sessionId: {}", request.getSessionId());

        // 1. Check duplicate + cleanup FAILED sessions (in transaction)
        transactionTemplate.executeWithoutResult(status -> handleDuplicateSession(request.getSessionId()));

        // 2. Save grading session PROCESSING (in transaction)
        GradingSession session = transactionTemplate.execute(status -> {
            GradingSession s = gradingMapper.toEntity(request);
            return sessionRepository.save(s);
        });

        // 3. Parallel LLM grading (NO transaction — avoid holding DB connections)
        List<GradingResult> results = gradeInParallel(request.getAnswers(), request.getTopic(), session.getId());

        // 4. Save results + update session (in transaction)
        transactionTemplate.executeWithoutResult(status -> {
            resultRepository.saveAll(results);
            session.setGradedCount(results.size());
            session.setStatus(GradingStatus.COMPLETED);
            sessionRepository.save(session);
        });

        // 5. Call Report Service (fire-and-forget, errors logged not thrown)
        reportClientService.createReport(session, results);

        // 6. Return response
        log.info("Grading completed for sessionId: {}, gradedCount: {}",
                request.getSessionId(), results.size());
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "grading", key = "#id")
    public GradingResponse getById(String id) {
        GradingSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GRD_001));
        List<GradingResult> results = resultRepository.findByGradingSessionId(id);
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "grading_session", key = "#sessionId")
    public GradingResponse getBySessionId(String sessionId) {
        GradingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.GRD_001));
        List<GradingResult> results = resultRepository.findByGradingSessionId(session.getId());
        return gradingMapper.toGradingResponse(session, results);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GradingSummaryResponse> getHistoryByUserId(Long userId, String status, Pageable pageable) {
        Page<GradingSession> page;
        if (status != null && !status.isBlank()) {
            GradingStatus gradingStatus;
            try {
                gradingStatus = GradingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(ErrorCode.GRD_004);
            }
            page = sessionRepository.findByUserIdAndStatus(userId, gradingStatus, pageable);
        } else {
            page = sessionRepository.findByUserId(userId, pageable);
        }
        return PageResponse.from(page.map(gradingMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GradingSummaryResponse> getAllGradings(Pageable pageable) {
        Page<GradingSession> page = sessionRepository.findAll(pageable);
        return PageResponse.from(page.map(gradingMapper::toSummaryResponse));
    }

    // --- Private helpers ---

    private void handleDuplicateSession(String sessionId) {
        // Allow re-grade if previous attempt FAILED
        sessionRepository.findBySessionIdAndStatus(sessionId, GradingStatus.FAILED)
                .ifPresent(failedSession -> {
                    log.info("Re-grading FAILED session: {}", sessionId);
                    resultRepository.deleteByGradingSessionId(failedSession.getId());
                    sessionRepository.delete(failedSession);
                });

        // Reject if PROCESSING or COMPLETED
        if (sessionRepository.existsBySessionIdAndStatusNot(sessionId, GradingStatus.FAILED)) {
            throw new DuplicateException(ErrorCode.GRD_002);
        }
    }

    private List<GradingResult> gradeInParallel(List<AnswerRequest> answers, String topic, String sessionId) {
        List<CompletableFuture<GradingResult>> futures = answers.stream()
                .map(answer -> CompletableFuture.supplyAsync(
                                () -> gradeOneQuestion(answer, topic, sessionId), gradingTaskExecutor)
                        .handle((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to grade question {}: {}", answer.getQuestionId(), ex.getMessage());
                                return buildFailedResult(answer, sessionId, ex.getMessage());
                            }
                            return result;
                        }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private GradingResult gradeOneQuestion(AnswerRequest answer, String topic, String sessionId) {
        long start = System.currentTimeMillis();
        String prompt = promptBuilder.build(answer, topic);
        LlmGradingResult llmResult = llmService.grade(prompt);
        long latencyMs = System.currentTimeMillis() - start;

        // Clamp score to valid range [0, maxScore]
        double clampedScore = Math.max(0, Math.min(llmResult.getScore(), answer.getMaxScore()));

        return GradingResult.builder()
                .gradingSessionId(sessionId)
                .questionId(answer.getQuestionId())
                .questionText(answer.getQuestionText())
                .answerText(answer.getAnswerText())
                .skillName(answer.getSkillName())
                .score(clampedScore)
                .maxScore(answer.getMaxScore())
                .feedback(llmResult.getFeedback())
                .promptUsed(prompt)
                .modelUsed(llmResult.getModel())
                .latencyMs(latencyMs)
                .build();
    }

    private GradingResult buildFailedResult(AnswerRequest answer, String sessionId, String errorMsg) {
        return GradingResult.builder()
                .gradingSessionId(sessionId)
                .questionId(answer.getQuestionId())
                .questionText(answer.getQuestionText())
                .answerText(answer.getAnswerText())
                .skillName(answer.getSkillName())
                .score(0.0)
                .maxScore(answer.getMaxScore())
                .feedback("Unable to grade: " + errorMsg)
                .modelUsed("N/A")
                .latencyMs(0L)
                .build();
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/service/GradingService.java src/main/java/org/aibles/intellihiregrading/service/impl/GradingServiceImpl.java
git commit -m "feat(grading): add core GradingService with parallel LLM grading and Report Service integration"
```

---

## Task 11: Controller

**Files:**
- Create: `controller/GradingController.java`

- [ ] **Step 1: Create GradingController.java**

```java
package org.aibles.intellihiregrading.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihiregrading.dto.*;
import org.aibles.intellihiregrading.exception.ErrorCode;
import org.aibles.intellihiregrading.exception.ForbiddenException;
import org.aibles.intellihiregrading.service.GradingService;
import org.aibles.intellihiregrading.utils.PaginationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/gradings")
@RequiredArgsConstructor
@Validated
public class GradingController {

    private final GradingService gradingService;

    @PostMapping
    public ResponseEntity<BaseResponse<GradingResponse>> grade(
            @Valid @RequestBody GradeRequest request) {
        log.info("Grading request for sessionId: {}", request.getSessionId());
        GradingResponse response = gradingService.grade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GradingResponse>> getById(@PathVariable String id) {
        log.info("Getting grading by id: {}", id);
        GradingResponse response = gradingService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<BaseResponse<GradingResponse>> getBySessionId(@PathVariable String sessionId) {
        log.info("Getting grading for sessionId: {}", sessionId);
        GradingResponse response = gradingService.getBySessionId(sessionId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<BaseResponse<PageResponse<GradingSummaryResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Getting grading history for userId: {}", userId);
        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.GRADING_SORTABLE_FIELDS);
        PageResponse<GradingSummaryResponse> response = gradingService.getHistoryByUserId(userId, status, pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<BaseResponse<PageResponse<GradingSummaryResponse>>> getAllGradings(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Admin getting all gradings, role: {}", userRole);
        if (!"ROLE_ADMIN".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new ForbiddenException(ErrorCode.GRD_005);
        }
        Pageable pageable = PaginationUtils.buildPageable(
                page, size, sort, PaginationUtils.GRADING_SORTABLE_FIELDS);
        PageResponse<GradingSummaryResponse> response = gradingService.getAllGradings(pageable);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/aibles/intellihiregrading/controller/
git commit -m "feat(grading): add GradingController with all REST endpoints"
```

---

## Task 12: Integration Tests

**Files:**
- Create: `src/test/java/org/aibles/intellihiregrading/controller/GradingControllerIntegrationTest.java`

- [ ] **Step 1: Create integration test**

```java
package org.aibles.intellihiregrading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aibles.intellihiregrading.dto.AnswerRequest;
import org.aibles.intellihiregrading.dto.GradeRequest;
import org.aibles.intellihiregrading.dto.openrouter.LlmGradingResult;
import org.aibles.intellihiregrading.service.LlmService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GradingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlmService llmService;

    @BeforeEach
    void setUp() {
        when(llmService.grade(anyString())).thenReturn(
                LlmGradingResult.builder()
                        .score(8.0)
                        .feedback("Good answer")
                        .model("test-model")
                        .build()
        );
    }

    @Nested
    class GradeEndpoint {

        @Test
        void grade_validRequest_returns201() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-1")
                    .userId(1L)
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder()
                                    .questionId("q1")
                                    .questionText("What is Java?")
                                    .answerText("A programming language")
                                    .skillName("java-core")
                                    .maxScore(10.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.session_id").value("test-session-1"))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.results[0].score").value(8.0));
        }

        @Test
        void grade_missingSessionId_returns400() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .userId(1L)
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").maxScore(10.0).build()
                    ))
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void grade_emptyAnswers_returns400() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-empty")
                    .userId(1L)
                    .answers(List.of())
                    .build();

            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void grade_duplicateSession_returns409() throws Exception {
            GradeRequest request = GradeRequest.builder()
                    .sessionId("test-session-dup")
                    .userId(1L)
                    .topic("Java")
                    .answers(List.of(
                            AnswerRequest.builder().questionId("q1").questionText("Q?")
                                    .answerText("A").skillName("java").maxScore(10.0).build()
                    ))
                    .build();

            // First call succeeds
            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Second call with same sessionId returns 409
            mockMvc.perform(post("/v1/gradings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class GetEndpoints {

        @Test
        void getById_notFound_returns404() throws Exception {
            mockMvc.perform(get("/v1/gradings/nonexistent-id"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getBySessionId_notFound_returns404() throws Exception {
            mockMvc.perform(get("/v1/gradings/session/nonexistent"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getHistory_returnsPage() throws Exception {
            mockMvc.perform(get("/v1/gradings/history/1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    class AdminEndpoint {

        @Test
        void adminGetAll_withoutRole_returns403() throws Exception {
            mockMvc.perform(get("/v1/gradings/admin/all"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void adminGetAll_withAdminRole_returns200() throws Exception {
            mockMvc.perform(get("/v1/gradings/admin/all")
                            .header("X-User-Role", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }
}
```

- [ ] **Step 2: Run all tests**

Run: `cd C:\Users\ACER\Desktop\aibless\intelli-hire-grading && mvn test -Dspring.profiles.active=test`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/
git commit -m "test(grading): add integration tests for GradingController"
```

---

## Task 13: Verify Full Build & Docker

- [ ] **Step 1: Full Maven build**

Run: `cd C:\Users\ACER\Desktop\aibless\intelli-hire-grading && mvn clean package -Dspring.profiles.active=test`
Expected: BUILD SUCCESS

- [ ] **Step 2: Start Docker infrastructure**

Run: `cd C:\Users\ACER\Desktop\aibless\intelli-hire-grading && docker-compose up -d`
Expected: PostgreSQL and Redis containers running

- [ ] **Step 3: Run application**

Run: `cd C:\Users\ACER\Desktop\aibless\intelli-hire-grading && mvn spring-boot:run`
Expected: Application starts on port 8084, Flyway migration runs, Swagger UI at http://localhost:8084/api/swagger-ui.html

- [ ] **Step 4: Test with curl**

```bash
curl -X POST http://localhost:8084/api/v1/gradings \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "test-001",
    "user_id": 1,
    "topic": "Java Spring Boot",
    "answers": [
      {
        "question_id": "q1",
        "question_text": "What is dependency injection?",
        "answer_text": "DI is a design pattern",
        "skill_name": "spring-core",
        "max_score": 10
      }
    ]
  }'
```
Expected: 201 response with grading results (requires valid OPENROUTER_API_KEY in .env)

- [ ] **Step 5: Final commit**

```bash
git add -A
git commit -m "feat(grading): complete Grading Service v1 - all endpoints, LLM integration, tests"
```
