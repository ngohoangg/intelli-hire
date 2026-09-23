# Code Review — intelli-hire-auth
*Reviewed: 2026-03-13*

---

## Executive Summary

The `intelli-hire-auth` service is a well-structured Spring Boot 3.4.2 authentication microservice built by a competent team. The codebase covers the full scope of the planned requirements: JWT lifecycle, OAuth2 (Google & GitHub), RBAC with seeded permissions, email verification, password reset, account locking, and admin management endpoints. Hexagonal architecture is adopted consistently, and numerous security best practices are correctly applied.

However, several gaps and issues — ranging from critical functional incompleteness to important architectural impurity — must be addressed before this service is ready for production traffic. The most serious problems are: a completely absent test suite, a broken email verification flow (the `email_verified` flag was recognized as missing but not resolved), plaintext token exposure in dev-mode API responses, an IP-header trust issue, and uncapped pagination that could destabilize the service under load.

---

## Strengths

1. **Hexagonal architecture applied consistently.** Every feature is decomposed into a use-case interface, an implementation, a command object, domain entities, repository adapters, and web DTOs. The dependency direction always points inward.
2. **JWT implementation is solid.** JJWT 0.12.6 is used correctly. Access and refresh tokens carry distinct `token_type` and `audience` claims, and the service validates both on every parse. Refresh-token rotation with pessimistic write-locking guards against race-condition replay attacks.
3. **Password-reset and verification tokens are hashed at rest.** `SecureTokenGenerator` generates a 256-bit random token and stores only its SHA-256 hex hash — a correct "token-as-proof" pattern.
4. **BCrypt strength is production-appropriate.** Cost factor 12 is a reasonable choice for 2026 hardware.
5. **All sensitive configuration is externalised.** `application.yml` contains no literal secrets; every sensitive value is read from environment variables via `spring-dotenv`.
6. **Error handling is centralised and consistent.** `GlobalExceptionHandler` + `ErrorCode` enum + `BaseException` hierarchy gives a uniform, machine-readable error format across all failure paths.
7. **Database schema is thorough.** 12 Flyway migrations cover every entity, every foreign-key relationship carries a meaningful `ON DELETE CASCADE`, and each table has the correct indexes for its common query patterns.
8. **RBAC seeding is idempotent.** The `ON CONFLICT DO NOTHING` clause in `assignDefaultRole` and the seed inserts using `SELECT … FROM roles/permissions WHERE name = 'X'` are safe to re-run.
9. **Refresh-token rotation revokes the old token before issuing a new one.** The pessimistic write-lock in `findByTokenIdWithLock` prevents two concurrent requests from both succeeding with the same token.
10. **GitHub OAuth handles private-email accounts.** `GitHubOAuthClient` falls back to the `/user/emails` endpoint and correctly prefers a primary+verified address.
11. **JPA auditing is correctly wired.** `JpaAuditingConfig` provides an `AuditorAware<Long>` that extracts the account ID from the `JwtAuthenticationToken`, so `created_by` / `updated_by` are populated automatically.

---

## Issues Found

### Critical

**C1 — Zero test coverage**
There is no `src/test` directory. No unit tests, no integration tests, no security tests. A service responsible for authentication and access control for an entire platform cannot ship without tests validating its JWT logic, account-locking thresholds, token rotation, RBAC enforcement, or any boundary conditions. This is the most significant gap in the codebase.

**C2 — Email verification has no effect on account access**
`VerifyEmailUseCaseImpl` marks the token as verified but explicitly acknowledges in a comment that the `AccountEntity` has no `email_verified` field and that nothing is done to the account status. As a result, any user can log in with an unverified email. The `ErrorCode.EMAIL_NOT_VERIFIED` value exists but is never thrown. Either the account schema must gain an `email_verified` column with enforcement in `LoginUseCaseImpl`, or the feature must be documented as deferred.

**C3 — Dev-mode token returned in HTTP response body**
`ForgotPasswordUseCaseImpl` returns the plain-text password-reset token in the API response when `spring.profiles.active` is `dev` or `local`. The `MessageResponseDTO` `token` field flows through `AuthController` and is serialised into the HTTP response body. If any logging proxy, API gateway, or browser developer-tools session captures this response in a pre-production environment (which often shares infrastructure with staging), the token leaks. The correct approach for development testing is to log the token only at DEBUG level server-side, never return it in the response body.

**C4 — Uncapped pagination allows denial-of-service**
`AdminListAccountsUseCaseImpl` accepts `size` directly from the caller with no upper bound. A single request with `size=1000000` will attempt to load the entire `accounts` table into memory. The `ListAccountsCommand` record and the controller should enforce a maximum page size (e.g., 100).

**C5 — Unverified X-Forwarded-For header trusted without restriction**
`getClientIp()` in `AuthController` and `OAuthController` blindly trusts the first value of `X-Forwarded-For`. An attacker can forge this header to bypass IP-based rate limiting or to associate a malicious IP with a legitimate request. The header should only be trusted when the request comes from a known, trusted proxy, or the service should rely solely on `RemoteAddr` and let a properly configured reverse proxy set a single, non-spoofable header.

---

### Important

**I1 — `BaseException` is declared abstract but is instantiated with anonymous subclasses throughout the codebase**
Dozens of use-case implementations throw `new BaseException(ErrorCode.XYZ) {}` — anonymous inline subclasses. This pattern defeats the purpose of the exception hierarchy. Every `ErrorCode` that can be thrown should have its own named exception class (following the pattern already established for `InvalidCredentialsException`, `AccountLockedException`, etc.). The anonymous subclass pattern causes unhelpful stack traces and cannot be caught selectively.

**I2 — `AdminListAccountsUseCaseImpl` ignores `status` and `search` filters**
`ListAccountsCommand` carries both a `status` filter and a `search` string, which the controller passes in. The implementation calls `accountRepository.findAll(pageable)` unconditionally, discarding both filters entirely. The admin list endpoint silently returns all accounts regardless of the parameters the caller provides, which is both a functional bug and a data-exposure risk.

**I3 — Password-strength policy is declared but never enforced**
`PasswordProperties` holds `minLength`, `requireUppercase`, `requireLowercase`, `requireDigit`, and `requireSpecial`. `RegisterRequestDTO` validates only a static `@Size(min=8)`. There is no validation service or custom constraint that applies the configurable policy. The configured rules are silently ignored at registration time.

**I4 — `LoginUseCaseImpl` checks `isInactive()` but not `isActive()` after password validation**
The login flow checks `isLocked()` first, then `isInactive()` before password validation. After a successful password match it calls `account.recordSuccessfulLogin()` without re-checking account state. The `RefreshTokenUseCaseImpl` performs `account.isActive()` at refresh time. These two code paths should use the same guard. More importantly, `isActive()` returns `true` only for `ACTIVE` status, but `isInactive()` covers only `INACTIVE`. A future status value would slip through silently.

**I5 — `AccountRoleEntity` maps `role_id` twice — as a column and as a `@ManyToOne`**
The entity defines `@Column(name = "role_id")` for the `roleId` field and `@ManyToOne` with `@JoinColumn(name = "role_id", insertable = false, updatable = false)` for the `role` navigation property. This is the correct JPA pattern, but in `AdminAccountController.toAccountRoleResponseDTO()` the code accesses `accountRole.getRole()` with a null-check, meaning Hibernate may perform a lazy-load on each element in the list — a textbook N+1 query. A `@EntityGraph` or `@Query` with a JOIN FETCH should be applied.

**I6 — `OAuthCallbackUseCaseImpl` silently links an OAuth identity to an existing password-based account without the account owner's consent**
When an OAuth login arrives with an email that already exists in the system, `handleNewOAuthUser` immediately creates an OAuth link to the existing account and proceeds to issue tokens. An attacker who knows a user's email address and can present a valid OAuth token from a compromised OAuth provider can hijack the account. The correct behaviour is to either reject the attempt or require the existing user to confirm the link via an authenticated session.

**I7 — `JwtTokenService.getSigningKey()` is called on every token operation**
`getSigningKey()` calls `jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)` and `Keys.hmacShaKeyFor(...)` on every invocation. The resulting `SecretKey` should be computed once at construction time and cached as a field, avoiding repeated byte-array allocation.

**I8 — `RestTemplate` used for OAuth HTTP calls; no retry or circuit-breaker**
`SimpleClientHttpRequestFactory` is a blocking, non-pooled HTTP client. For production usage, `HttpComponentsClientHttpRequestFactory` backed by a connection pool (e.g., Apache HttpClient 5) should be used. Additionally, transient OAuth provider failures during code exchange will surface as unhandled `OAuthCallbackException` with raw external error messages.

**I9 — No scheduled cleanup of expired tokens**
`RefreshTokenRepository.deleteExpiredTokens`, `PasswordResetTokenRepository.deleteExpiredTokens`, and `EmailVerificationTokenRepository.deleteExpiredTokens` are defined but never called. There is no `@Scheduled` job to invoke them. Over time the token tables will grow unboundedly.

**I10 — `AccountEntity` uses `LocalDateTime.now()` inside domain methods**
Methods such as `lock()`, `unlock()`, `incrementFailedLoginAttempts()`, and `recordSuccessfulLogin()` call `LocalDateTime.now()` directly. This makes the domain behaviour untestable without clock manipulation and ties the entity to the system clock. A `Clock` dependency (or passing the timestamp as a parameter) is the standard solution.

**I11 — Account-locking auto-unlock by duration is configured but not implemented**
`PasswordProperties.lockDurationMinutes` implies that accounts should automatically unlock after the configured duration. The code never reads `lockDurationMinutes` to unlock accounts on login attempts. The `lockedAt` timestamp is stored but is never compared against the configured duration. The feature is declared in configuration but has no implementation.

---

### Minor

**m1 — Duplicate `getClientIp()` method**
`getClientIp()` is copy-pasted into `AuthController` and `OAuthController`. The `OAuthController` version also omits the `X-Real-IP` fallback present in `AuthController`. This utility should be extracted to a shared static helper or a request-scoped component.

**m2 — `AccountRepository` defines two overloaded `findByEmailOrUsername` methods**
The interface declares the Spring-Data-derived method `findByEmailOrUsername(String email, String username)` (two-parameter) and the custom `@Query`-annotated `findByEmailOrUsername(@Param("identifier") String identifier)` (one-parameter). The two-parameter form will never be called correctly for the login use case. The naming ambiguity may cause confusion and future misuse.

**m3 — `@ResponseStatus(HttpStatus.CREATED)` on `register` is contradicted by `ResponseEntity.status(HttpStatus.CREATED)`**
`AuthController.register` is annotated with `@ResponseStatus(HttpStatus.CREATED)` and also manually sets `HttpStatus.CREATED` on the `ResponseEntity`. The annotation is redundant and misleading when the method returns a `ResponseEntity`; Spring will use the `ResponseEntity` status.

**m4 — `assign role` endpoint returns `200 OK` instead of `201 Created`**
`AdminAccountController.assignRole` returns `ResponseEntity.ok().build()` after a POST that creates a new resource. The HTTP specification and REST conventions call for `201 Created`, optionally with a `Location` header.

**m5 — `unlink` endpoint does not guard against removing the user's only authentication method**
`UnlinkOAuthProviderUseCaseImpl` removes the OAuth link unconditionally. If the account has no password (set to `null` for OAuth-only accounts) and this is the last linked provider, the user will be permanently locked out of their account.

**m6 — `SHA-256` for token hashing is not keyed**
`SecureTokenGenerator.hashToken` uses plain SHA-256 without a HMAC. While the random token itself has 256 bits of entropy (making pre-image attacks infeasible), a keyed HMAC-SHA-256 is the standard for token hashing in security-sensitive contexts because it provides length-extension-attack resistance and is consistent with how JWTs are signed.

**m7 — `application.yml` enables debug-level SQL and security logging unconditionally**
`org.springframework.security: DEBUG` and `org.hibernate.SQL: DEBUG` are set at the root `application.yml` level with no profile guard. In production, these logs will expose SQL statements and every security decision to log aggregators.

**m8 — MapStruct declared as a dependency but not actually used**
The `pom.xml` declares MapStruct and its annotation processor, and the compiler plugin is correctly configured. However, no `@Mapper`-annotated interfaces exist in the codebase. All DTO mappings are performed by hand inside controller methods. MapStruct should either be used or removed from the dependency list to avoid misleading future contributors.

**m9 — `PasswordProperties.@Validated` does not register the properties as a `@Bean`**
`JwtProperties`, `PasswordProperties`, and `OAuth2Properties` use `@ConfigurationProperties` and `@Validated`, but there is no `@EnableConfigurationProperties(...)` or `@Bean` annotation that registers them. They must be either annotated with `@Component` or listed in `@EnableConfigurationProperties` on the application class. (This may work if Spring Boot auto-detects them via the `spring-boot-configuration-processor`, but the intent is ambiguous.)

**m10 — `account_roles` schema contains `assigned_by`, `updated_at`, and `updated_by` columns not mapped by `AccountRoleEntity`**
The V004 migration creates `assigned_by`, `updated_at`, and `updated_by` columns. `AccountRoleEntity` maps none of them. Hibernate validates the schema on startup (`ddl-auto: validate`), which means these extra columns will pass validation (Hibernate only checks that mapped columns exist, not that unmapped columns don't), but the audit data will never be written.

**m11 — `UnlockAccountUseCase` is exposed under no controller**
`UnlockAccountUseCaseImpl` is implemented and the use case interface exists, but there is no admin controller endpoint that calls it. The `AdminUpdateAccountStatusUseCaseImpl` covers locking/unlocking via status changes, making `UnlockAccountUseCase` a dead code path.

---

## Detailed Analysis

### Architecture

The package structure follows hexagonal architecture cleanly: each bounded context (`account`, `auth`, `oauth`, `profile`, `rbac`, `admin`) contains `domain/`, `adapter/` (split into `web/` and `repository/`), and `usecase/` sub-packages. Commands are value objects (Java records) that flow from the adapter layer into use cases. Use-case interfaces are the ports; implementations are the application core; JPA repositories and REST controllers are the adapters.

One impurity breaks the model: `AdminAccountController` directly imports `AccountEntity` and `AccountRoleEntity` from the domain layer and maps them to DTOs inline. In strict hexagonal terms the controller should receive a use-case result (a DTO or a result record) rather than a raw entity, because the entity is a persistence concern. This coupling means that adding a JPA association to the entity (e.g., for eager loading) can silently change what the controller serialises.

The `shared` package serves as a cross-cutting concern layer for config, exception infrastructure, and utilities. This is a pragmatic and acceptable choice.

The `auth.usecase` package mixes use-case interfaces/impls with inner record types (`LoginResponse`, `RegisterResponse`, etc.). Moving these result types to a dedicated `auth.usecase.result` package, or keeping them as inner records inside the interface, is a minor style issue but the current approach where the inner records live in the interface file is readable and consistent.

### Security

**JWT:** Access and refresh tokens are correctly separated by `token_type` claim and distinct audiences. Token expiration, issuer, and audience are all verified on every parse. Refresh-token rotation is implemented. Stateless session management is configured. CSRF is disabled (appropriate for stateless APIs consumed by non-browser clients).

**Concern:** `JwtTokenService.getSigningKey()` re-derives the `SecretKey` from the raw bytes on every call. While not a security vulnerability, it is a correctness concern because the JJWT documentation recommends holding the key as a field.

**OAuth state parameter:** The state value is generated server-side in `OAuthController.getAuthorizationUrl()` as a `UUID` and returned to the client, but it is **not stored or validated on callback**. The `OAuthCallbackCommand` carries the `state` field from the request, but `OAuthCallbackUseCaseImpl` never checks it against anything. This completely undermines the CSRF protection that the state parameter is meant to provide. An attacker can initiate a login, discard the state, and force the victim's browser to complete the flow.

**Password hashing:** BCrypt(12) is correct.

**Token hashing at rest:** SHA-256 is used for password-reset and email-verification tokens. The plain token is never persisted. This is correct.

**Account locking:** The locking threshold is configurable and the mechanism works. The configured auto-unlock duration is never applied (see I11).

**Security filter chains:** The multi-chain approach using `@Order` is technically correct but brittle. The `defaultSecurityFilterChain` at `@Order(4)` closes with `.anyRequest().authenticated()`, which means any URL not matched by the earlier chains also requires authentication. This is a safe default, but it means that if a new public endpoint is added and not listed in `publicSecurityFilterChain`, it will silently become authenticated-only.

**Actuator exposure:** `health` and `info` endpoints are public. `metrics` is listed in `include` but no matcher in `docsSecurityFilterChain` covers `/actuator/metrics`. Combined with the `anyRequest().authenticated()` default chain, metrics will require authentication — which is the intended behaviour, but it is implicit rather than explicit.

### Code Quality

Naming is consistent and descriptive. Lombok is used appropriately (`@RequiredArgsConstructor`, `@Getter`, `@Setter`, `@Slf4j`). Java records are correctly used for immutable command objects.

The anonymous `BaseException` subclass pattern (e.g., `new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND) {}`) is the most pervasive code-quality issue. It appears in `LoginUseCaseImpl`, `RefreshTokenUseCaseImpl`, `ResetPasswordUseCaseImpl`, `VerifyEmailUseCaseImpl`, and others. Named exception classes should replace every anonymous instantiation.

`@Data` is used on `RegisterRequestDTO`. For request DTOs, `@Data` generates both getters and setters. Setters on request objects are rarely needed after deserialization and can allow accidental mutation. `@Getter` + `@NoArgsConstructor` (or `@Value` for immutable DTOs) is preferable.

The `encode()` URL-encoding helper is copy-pasted between `GoogleOAuthClient` and `GitHubOAuthClient`. It should be a shared static method in the `OAuthClient` interface (as a default method) or in a utility class.

### Error Handling

The `GlobalExceptionHandler` covers: `BaseException` (business errors), `MethodArgumentNotValidException` (validation), `AccessDeniedException`, `AuthenticationException`, and the generic `Exception` fallback. The error response format (`ErrorResponse`) includes a machine-readable code, an HTTP status, a human-readable message, optional detail string, and optional field errors. This is a well-designed contract.

One gap: `HttpMessageNotReadableException` (malformed JSON body) is not handled. It will fall through to the generic `Exception` handler, returning a 500 with the generic error code instead of a 400. Spring's `ResponseEntityExceptionHandler` handles this by default but `GlobalExceptionHandler` does not extend it.

The `InvalidTokenException` in `JwtAuthenticationFilter` is caught and results in an empty `SecurityContext`. The filter then lets the request continue. Downstream the security configuration will reject it with a 401 via `HttpStatusEntryPoint`. This is correct, but the 401 response body in that path is empty rather than using the structured `ErrorResponse` format, because `HttpStatusEntryPoint` sets the status code only. A custom `AuthenticationEntryPoint` that writes a structured JSON body would be consistent.

### Database & Migrations

All 12 Flyway migrations are well-written. Every foreign key has a sensible cascade rule. The use of `BIGINT GENERATED BY DEFAULT AS IDENTITY` is the correct PostgreSQL identity column pattern.

**Index coverage is good** for the query patterns visible in the repositories:
- `accounts`: index on `email`, `username`, `status`
- `refresh_tokens`: index on `token_id`, `account_id`, `expires_at`
- `password_reset_tokens`: index on `token_hash`, `account_id`, `expires_at`
- `account_oauth_links`: composite index on `(provider_id, provider_user_id)`

One observation: `email_verification_tokens` does not index `expires_at`, while `password_reset_tokens` and `refresh_tokens` do. The `deleteExpiredTokens` query on email-verification tokens will do a full table scan. This is a minor inconsistency.

The `V002__create_user_profiles.sql` migration is referenced but not read directly; it should be reviewed to ensure the `phone UNIQUE` constraint matches the entity definition on `UserProfileEntity` (the entity declares `unique = true` on `phone`, which is correct but unusual — most profiles allow the same phone to be registered more than once if an account is deleted and recreated).

### API Design

REST conventions are generally followed. HTTP methods are correctly chosen (GET for reads, POST for creates, PATCH for partial updates, DELETE for deletes). Versioning via `/v1/` prefix is present.

The `resend-verification` endpoint is declared in `SecurityConfig` as a public URL but no controller method or use case implements it. It is a dead entry in the security permit list.

`POST /v1/auth/logout-all` exists in the controller but is not declared as a permitted URL in any security chain matcher. It falls to the `defaultSecurityFilterChain` which requires authentication — correct behaviour — but this dependency on the catch-all chain is fragile.

`RegisterRequestDTO` does not mark `firstName` and `lastName` as `@NotBlank`. For a hiring platform, these are arguably required fields; the current schema permits empty profiles.

### Configuration

All sensitive values are externalised correctly. `JwtProperties` and `PasswordProperties` use `@Validated` with JSR-303 constraints, which will cause startup failure if required values are missing or below minimum thresholds — a good fail-fast pattern.

`application.yml` enables `org.springframework.security: DEBUG` and `org.hibernate.SQL: DEBUG` unconditionally. This is acceptable for a development branch but must be removed or placed behind a profile before staging deployment.

`hikari.maximum-pool-size: 10` is a reasonable default for a single-instance service. The `minimum-idle: 2` keeps two warm connections. This configuration is adequate for moderate load.

`baseline-on-migrate: true` in Flyway should be reviewed. On a fresh database this has no effect, but on an existing database without a Flyway baseline it can cause unintended skipping of migrations. For a new service this setting should be `false`.

### Testing

There is no `src/test` directory. This is the highest-priority gap. The following tests are minimally required before production:

- Unit tests for `JwtTokenService` (issue, parse, validate, type mismatch, audience mismatch, expired)
- Unit tests for `LoginUseCaseImpl` (correct credentials, wrong password, locked account, inactive account, auto-lock threshold)
- Unit tests for `RefreshTokenUseCaseImpl` (rotation, revoked token, expired token, account mismatch)
- Unit tests for `SecureTokenGenerator` (token length, hash stability)
- Unit tests for `PasswordProperties`-based validation (when eventually wired)
- Integration tests for `AuthController` using `MockMvc` + H2 or `@SpringBootTest` with Testcontainers
- Security integration tests verifying that admin endpoints reject non-ADMIN tokens

### Performance

**N+1 query risk in `AdminGetAccountRolesUseCaseImpl` → `AdminAccountController.toAccountRoleResponseDTO`:** The `AccountRoleEntity` has a `@ManyToOne(fetch = FetchType.LAZY)` to `RoleEntity`. When the controller iterates the list and calls `accountRole.getRole().getName()`, each access fires an individual SELECT unless a JOIN FETCH or entity graph was used to load the list. A `@Query("SELECT ar FROM AccountRoleEntity ar JOIN FETCH ar.role WHERE ar.accountId = :accountId")` in the repository would eliminate this.

**Two separate database roundtrips for roles and permissions at login:** `LoginUseCaseImpl` calls `accountAuthorizationRepository.findRolesByAccountId` and then `accountAuthorizationRepository.findPermissionsByAccountId` as two sequential queries. These could be combined into a single query that returns both, but this is a minor optimisation.

**`ensureUniqueUsername` busy-loop:** `OAuthCallbackUseCaseImpl.ensureUniqueUsername` loops `accountRepository.existsByUsername(username)` with incrementing suffixes. Under concurrent OAuth registrations for the same base name, this loop has no termination guarantee and can issue many SELECT queries. A single INSERT with a unique-constraint retry (or a database-generated unique suffix) would be more robust.

---

## Recommendations & Next Steps

1. **Add tests immediately.** Begin with unit tests for `JwtTokenService` and `LoginUseCaseImpl`, then add Spring Security integration tests using MockMvc. Aim for at minimum 70% line coverage on use-case and token-service code before the first production deployment.

2. **Implement email verification enforcement.** Add `email_verified BOOLEAN NOT NULL DEFAULT FALSE` to `accounts` via a new Flyway migration, toggle it in `VerifyEmailUseCaseImpl`, and enforce it in `LoginUseCaseImpl`.

3. **Remove the dev-mode token from the response body.** Log it server-side at DEBUG level only. Provide a developer convenience via an admin endpoint (protected by ADMIN role) or a separate test harness.

4. **Fix the OAuth state validation.** Store the generated `state` value (in a short-lived cache, Redis, or the database) when the authorization URL is issued, and validate it in `OAuthCallbackUseCaseImpl` before exchanging the code.

5. **Replace all anonymous `BaseException` instantiations** with named exception classes. Follow the existing pattern of `InvalidCredentialsException`, `AccountLockedException`, etc.

6. **Implement the `status` and `search` filters in `AdminListAccountsUseCaseImpl`** and enforce a maximum page size.

7. **Implement the auto-unlock duration.** In `LoginUseCaseImpl`, when a locked account is encountered, check `account.getLockedAt().plusMinutes(passwordProperties.getLockDurationMinutes()).isBefore(LocalDateTime.now())` and unlock automatically before proceeding.

8. **Wire password-strength validation.** Create a `PasswordValidator` component that reads `PasswordProperties` and apply it in `RegisterUseCaseImpl` and `ResetPasswordUseCaseImpl`.

9. **Add `@Scheduled` token cleanup jobs.** Schedule `deleteExpiredTokens` for refresh, password-reset, and email-verification tables. A nightly or hourly run is appropriate.

10. **Cache the `SecretKey` in `JwtTokenService`.** Compute `getSigningKey()` once in `@PostConstruct` and store it as a private field.

11. **Protect the account-linking flow.** When an OAuth callback email matches an existing account, reject the implicit link. Instead, require the user to authenticate their existing account first and then explicitly link from an authenticated session.

12. **Replace `SimpleClientHttpRequestFactory` with a pooled HTTP client** for the OAuth REST calls. Add timeout configuration at the property level.

13. **Remove debug-level logging from the base `application.yml`** and restrict it to a `dev` profile.

14. **Remove or implement `UnlockAccountUseCase`** — it is dead code. The same functionality is covered by `AdminUpdateAccountStatusUseCase`.

15. **Guard `UnlinkOAuthProviderUseCase`** against removing the user's only authentication method.

---

## Assessment

| Dimension              | Score | Notes |
|------------------------|-------|-------|
| Architecture           | 8/10  | Hexagonal pattern applied consistently; minor entity-leak to controller |
| Security               | 5/10  | Multiple critical gaps: no OAuth state validation, missing email enforcement, IP spoofing |
| Code Quality           | 6/10  | Good naming and structure; anonymous exception pattern is pervasive |
| Error Handling         | 7/10  | Centralised and consistent; gaps in 401 body and `HttpMessageNotReadableException` |
| Database & Migrations  | 8/10  | Well-structured schema; good indexes; minor missing index and orphan columns |
| API Design             | 7/10  | Mostly correct REST; a few HTTP status inconsistencies; dead security entries |
| Configuration          | 7/10  | Properly externalised; debug logging in production profile is a risk |
| Testing                | 0/10  | No tests exist |
| Performance            | 6/10  | N+1 risk, uncapped pagination, non-pooled HTTP client |

**Overall Score: 6/10**

**Ready for production: No**

The service demonstrates a solid architectural foundation and covers the planned feature scope. However, the complete absence of tests, the non-functional email-verification enforcement, the missing OAuth state validation, and the uncapped pagination issue make it unsuitable for production deployment in its current state. With approximately two to three focused sprints addressing the critical and important issues above — particularly the test suite and the security gaps — the service would be a strong candidate for a production release.
