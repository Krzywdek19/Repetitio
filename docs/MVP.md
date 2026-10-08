# Repetitio MVP

## Goal

Build a secure REST API for a learning application.

The first version should provide:

- user registration
- user login
- JWT authentication
- notes
- flashcards
- basic spaced repetition
- concurrency protection
- proper authorization and ownership checks

RAG and AI features are **not** part of this MVP.

---

## Technology

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Nimbus JOSE + JWT
- Testcontainers
- JUnit 5

---

## Architecture

Use a modular monolith organized by feature.

Suggested modules:

- auth
- user
- notes
- flashcards
- study

Shared infrastructure may contain:

- security
- exception handling

Keep modules loosely coupled.

Use DTOs at API and module boundaries.

Do not expose JPA entities through REST APIs.

Prefer ports and adapters where external infrastructure is involved.

Do not introduce unnecessary abstractions.

---

## Authentication

### Registration

`POST /api/auth/register`

User:

- id
- email
- passwordHash
- createdAt

Email must be unique.

Race condition during registration must be handled using:

- application-level existence check for good UX
- `UNIQUE` constraint in PostgreSQL as final protection
- proper handling of database constraint violations

Add a concurrency integration test proving that only one concurrent registration for the same email succeeds.

### Login

`POST /api/auth/login`

Return an access token.

Use JWT authentication with Nimbus.

JWT should contain at least:

- `sub`
- `iss`
- `iat`
- `exp`
- `jti`

Use asymmetric signing, preferably `RS256`.

Private keys and secrets must never be committed.

Refresh tokens are not part of the MVP.

---

## Notes

Note:

- id
- ownerId
- title
- content
- createdAt
- updatedAt

Endpoints:

- `POST /api/notes`
- `GET /api/notes`
- `GET /api/notes/{id}`
- `PATCH /api/notes/{id}`
- `DELETE /api/notes/{id}`

Users must only be able to access their own notes.

Ownership must be enforced on the backend.

---

## Flashcards

Flashcard:

- id
- ownerId
- noteId nullable
- front
- back
- createdAt
- updatedAt

Endpoints:

- `POST /api/flashcards`
- `GET /api/flashcards`
- `GET /api/flashcards/{id}`
- `PATCH /api/flashcards/{id}`
- `DELETE /api/flashcards/{id}`

A flashcard may optionally belong to a note.

Users may only access their own flashcards.

---

## Study / Spaced Repetition

Create a separate scheduling model.

FlashcardSchedule:

- id
- flashcardId
- nextReviewAt
- lastReviewedAt
- intervalDays
- repetitionCount
- easeFactor
- version

Use optimistic locking for concurrent review updates.

Endpoint:

`GET /api/study/due`

Returns flashcards currently due for review.

Review endpoint:

`POST /api/study/flashcards/{id}/review`

Rating:

- `AGAIN`
- `HARD`
- `GOOD`
- `EASY`

The first implementation may use a simple scheduling algorithm.

FSRS and advanced algorithms are out of scope for the MVP.

---

## Concurrency

The MVP should demonstrate two different concurrency protections.

### Registration

Use a database `UNIQUE` constraint to protect against concurrent registration with the same email.

### Flashcard review

Use optimistic locking with `@Version` to protect `FlashcardSchedule` against lost updates.

Add meaningful integration tests for both cases.

---

## Security

Security is a first-class requirement.

At minimum:

- authenticated endpoints must require JWT
- passwords must be hashed using BCrypt
- users must never access another user's resources
- validate all external input
- never expose password hashes
- never commit secrets or private keys
- avoid trusting resource IDs supplied by the client without ownership validation
- keep JWT signing and verification configuration isolated from business logic
- prefer Spring Security's JWT support with Nimbus rather than custom token parsing

---

## Build and Verification

Use Maven Wrapper when available.

Typical commands:

```bash
./mvnw test
./mvnw verify
```

On Windows:

```powershell
mvnw.cmd test
mvnw.cmd verify
```

---

## Out of scope

Do not implement yet:

- RAG
- embeddings
- vector database
- pgvector
- OpenAI integration
- document upload
- AI-generated flashcards
- AI-generated notes
- frontend
- Redis
- microservices
- OAuth login
- refresh tokens
- email notifications
