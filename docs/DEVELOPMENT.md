# Running and reviewing the MVP

## Prerequisites

- Java 21
- Maven Wrapper (included)
- PostgreSQL for a running application
- Docker for integration tests (Testcontainers starts PostgreSQL 17 Alpine)

The application retains Spring Boot 4.1.1 and Maven.
There is a pending compatibility decision: Boot 4.1.1's Spring test extension requires
a newer JUnit API than JUnit 5.14.1. Unit tests run on JUnit 5, but Spring integration
tests currently fail during bootstrap. Do not treat the MVP as fully verified until
this decision is resolved and the integration suite passes.

## Configuration

Supply these environment variables through your development environment or secret manager:

| Variable | Value |
| --- | --- |
| DB_URL | JDBC PostgreSQL URL, e.g. jdbc:postgresql://localhost:5432/repetitio |
| DB_USERNAME | Database login |
| DB_PASSWORD | Database password |
| JWT_PRIVATE_KEY | Complete PKCS#8 PEM RSA private-key text |
| JWT_PUBLIC_KEY | Complete X.509 PEM RSA public-key text |
| JWT_ISSUER | Optional; defaults to repetitio |
| JWT_AUDIENCE | Optional; defaults to repetitio-api |

Keys must match and be at least 2048 bits. Generate and store development keys outside
the repository. The application does not generate production signing keys or provide
fallback secrets. Environment variables contain actual PEM text, not file paths.
PEM/key and local .env files are ignored, but secrets must still be kept out of tracked files.

PowerShell example for loading existing, externally stored keys:

```powershell
$env:JWT_PRIVATE_KEY = Get-Content -Raw -LiteralPath 'C:\private\repetitio-private.pem'
$env:JWT_PUBLIC_KEY = Get-Content -Raw -LiteralPath 'C:\private\repetitio-public.pem'
.\mvnw.cmd spring-boot:run
```

Flyway applies versioned migrations automatically. Hibernate validates the schema and
does not create it. Open Session in View is disabled.
Never change an already-applied migration; add a new migration instead.

## Verification

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Surefire runs unit tests; Failsafe runs *IT integration tests during verify.
Integration tests generate disposable RSA keys in memory and use an isolated PostgreSQL
container. They must fail when Docker is unavailable rather than silently skip database checks.

- SchedulerTest: rating transitions and scheduling bounds.
- JwtValidationTest: required claims, issuer/audience, expiry, signatures and algorithms.
- RequestSizeFilterTest: request limits including unknown Content-Length.
- DatabaseIT: Flyway startup on PostgreSQL.
- ApiIT: authentication, validation, CRUD, two-user isolation, PATCH, deletion and reviews.
- ConcurrencyIT: barriers force both registrations through the existence check and both
  reviews through the same schedule read; also checks schedule/card rollback.
- DueQueryIT: exact due-time boundaries, ownership and stable pagination.

Do not share a development database with the tests.

## Module boundaries and transactions

The root package is pl.exceptionhandled.repetitio.

- auth depends on user application contracts. BCrypt and Nimbus are adapters.
- user owns account persistence. Named email-constraint failures are translated in its
  persistence adapter, unwind the transaction, and become HTTP 409 in auth.
- notes owns notes. flashcards calls the notes application service to validate associations.
- study depends on flashcards application contracts and its FlashcardCreated event.
- shared contains security plumbing, pagination, configuration, and safe errors.
- JPA entities/repositories stay inside each module's persistence package.
- The scheduling domain is pure Java.

Application services define transaction boundaries. The synchronous FlashcardCreated
listener requires an existing transaction and inserts exactly one schedule.
Failure rolls back both inserts. V3 backfills schedules for cards created before that migration.

Study's read adapter uses one parameterized SQL join to avoid loading other modules'
entities or causing per-card queries. Foreign-key actions detach deleted notes and
cascade deleted cards to schedules. These are intentional database-level dependencies.

Reviews compare the client's expectedVersion and use JPA @Version to reject overlapping
writes. The update is flushed before returning the incremented version.
Other CRUD edits intentionally retain last-write-wins semantics.

## Before public deployment

Terminate TLS and configure request timeouts and login/registration rate limiting at the
deployment edge. BCrypt cost 12 is an initial setting that should be benchmarked on the
deployment hardware. Keep CORS restricted to explicit origins when a browser client exists.
The API uses Authorization headers, not authentication cookies; revisit CSRF protection
if that changes.

Tokens expire after 15 minutes and cannot be individually revoked in this MVP.
Coordinate signing-key changes with that lifetime; automatic rotation is not implemented.
Duplicate registration responses intentionally disclose that an email is registered.

RAG, uploads, AI, refresh tokens, Redis, frontend work, and account deletion remain out of scope.
