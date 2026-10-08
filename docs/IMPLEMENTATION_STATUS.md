# Implementation status

The MVP code is implemented but has NOT passed PostgreSQL integration verification yet.

- Maven and Spring Boot 4.1.1 retained.
- Latest unit run: `mvnw.cmd -q test` — 18 tests, no failures.
- Integration verification is blocked by the JUnit 5 / Spring Boot 4.1.1 test-extension incompatibility.
- Pending user decision: use Boot-managed JUnit 6, or select a Boot version compatible with the required JUnit 5.
- No commits were created. Existing AGENTS.md, MVP.md, skills, and wrapper files were preserved.

## Review steps

| Step | Code | Verification |
| --- | --- | --- |
| Foundation | Maven dependencies, Flyway, external configuration, PostgreSQL fixture | Compiles; database test blocked at JUnit bootstrap |
| Accounts/authentication | Normalized unique email, BCrypt, login, Nimbus JWT | JWT unit tests pass; account/API/concurrency tests prepared |
| Notes/cards | Owner-scoped CRUD, optional note association, PATCH, deletion rules | Compiles; API integration tests prepared |
| Study | Atomic schedule creation, pure scheduling rules, due query, versioned reviews | Scheduling unit tests pass; PostgreSQL/concurrency tests prepared |
| Security/review | Body limit, safe errors, credential redaction, module boundary review | Request-limit unit tests pass; integration validation still required |
| Documentation | API contract and development guide | Reviewed against implementation |

## Files changed in this implementation

Modified existing files:

- pom.xml
- .gitignore
- src/main/resources/application.yaml

Removed/replaced:

- src/test/java/pl/exceptionhandled/repetitio/RepetitioApplicationTests.java (replaced with PostgreSQL integration fixtures)

New files:

- src/main/java/pl/exceptionhandled/repetitio/auth/api/AuthController.java
- src/main/java/pl/exceptionhandled/repetitio/auth/api/CredentialsRequest.java
- src/main/java/pl/exceptionhandled/repetitio/auth/application/AccessTokenIssuer.java
- src/main/java/pl/exceptionhandled/repetitio/auth/application/AuthService.java
- src/main/java/pl/exceptionhandled/repetitio/auth/application/PasswordHasher.java
- src/main/java/pl/exceptionhandled/repetitio/auth/application/TokenResponse.java
- src/main/java/pl/exceptionhandled/repetitio/auth/infrastructure/BCryptPasswordHasher.java
- src/main/java/pl/exceptionhandled/repetitio/auth/infrastructure/NimbusAccessTokenIssuer.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/api/CreateFlashcardRequest.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/api/FlashcardController.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/api/UpdateFlashcardRequest.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/application/FlashcardCreated.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/application/FlashcardData.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/application/FlashcardRepository.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/application/FlashcardService.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/infrastructure/persistence/FlashcardEntity.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/infrastructure/persistence/FlashcardPersistenceAdapter.java
- src/main/java/pl/exceptionhandled/repetitio/flashcards/infrastructure/persistence/JpaFlashcardRepository.java
- src/main/java/pl/exceptionhandled/repetitio/notes/api/CreateNoteRequest.java
- src/main/java/pl/exceptionhandled/repetitio/notes/api/NoteController.java
- src/main/java/pl/exceptionhandled/repetitio/notes/api/UpdateNoteRequest.java
- src/main/java/pl/exceptionhandled/repetitio/notes/application/NoteData.java
- src/main/java/pl/exceptionhandled/repetitio/notes/application/NoteRepository.java
- src/main/java/pl/exceptionhandled/repetitio/notes/application/NoteService.java
- src/main/java/pl/exceptionhandled/repetitio/notes/infrastructure/persistence/JpaNoteRepository.java
- src/main/java/pl/exceptionhandled/repetitio/notes/infrastructure/persistence/NoteEntity.java
- src/main/java/pl/exceptionhandled/repetitio/notes/infrastructure/persistence/NotePersistenceAdapter.java
- src/main/java/pl/exceptionhandled/repetitio/shared/api/PageResponse.java
- src/main/java/pl/exceptionhandled/repetitio/shared/config/TimeConfiguration.java
- src/main/java/pl/exceptionhandled/repetitio/shared/error/ApiException.java
- src/main/java/pl/exceptionhandled/repetitio/shared/error/ApiExceptionHandler.java
- src/main/java/pl/exceptionhandled/repetitio/shared/security/CurrentUser.java
- src/main/java/pl/exceptionhandled/repetitio/shared/security/JwtConfiguration.java
- src/main/java/pl/exceptionhandled/repetitio/shared/security/RequestSizeFilter.java
- src/main/java/pl/exceptionhandled/repetitio/shared/security/SecurityConfiguration.java
- src/main/java/pl/exceptionhandled/repetitio/study/api/ReviewRequest.java
- src/main/java/pl/exceptionhandled/repetitio/study/api/StudyController.java
- src/main/java/pl/exceptionhandled/repetitio/study/application/DueFlashcard.java
- src/main/java/pl/exceptionhandled/repetitio/study/application/DueFlashcardQuery.java
- src/main/java/pl/exceptionhandled/repetitio/study/application/ScheduleData.java
- src/main/java/pl/exceptionhandled/repetitio/study/application/ScheduleRepository.java
- src/main/java/pl/exceptionhandled/repetitio/study/application/StudyService.java
- src/main/java/pl/exceptionhandled/repetitio/study/domain/Rating.java
- src/main/java/pl/exceptionhandled/repetitio/study/domain/ScheduleState.java
- src/main/java/pl/exceptionhandled/repetitio/study/domain/Scheduler.java
- src/main/java/pl/exceptionhandled/repetitio/study/infrastructure/persistence/FlashcardScheduleEntity.java
- src/main/java/pl/exceptionhandled/repetitio/study/infrastructure/persistence/JdbcDueFlashcardQuery.java
- src/main/java/pl/exceptionhandled/repetitio/study/infrastructure/persistence/JpaScheduleRepository.java
- src/main/java/pl/exceptionhandled/repetitio/study/infrastructure/persistence/SchedulePersistenceAdapter.java
- src/main/java/pl/exceptionhandled/repetitio/user/application/AuthenticationCredentials.java
- src/main/java/pl/exceptionhandled/repetitio/user/application/EmailAlreadyRegisteredException.java
- src/main/java/pl/exceptionhandled/repetitio/user/application/UserIdentity.java
- src/main/java/pl/exceptionhandled/repetitio/user/application/UserRepository.java
- src/main/java/pl/exceptionhandled/repetitio/user/application/UserService.java
- src/main/java/pl/exceptionhandled/repetitio/user/infrastructure/persistence/JpaUserRepository.java
- src/main/java/pl/exceptionhandled/repetitio/user/infrastructure/persistence/UserEntity.java
- src/main/java/pl/exceptionhandled/repetitio/user/infrastructure/persistence/UserPersistenceAdapter.java
- src/main/resources/db/migration/V1__users.sql
- src/main/resources/db/migration/V2__notes_and_flashcards.sql
- src/main/resources/db/migration/V3__flashcard_schedules.sql
- src/test/java/pl/exceptionhandled/repetitio/ApiIT.java
- src/test/java/pl/exceptionhandled/repetitio/ConcurrencyIT.java
- src/test/java/pl/exceptionhandled/repetitio/DatabaseIT.java
- src/test/java/pl/exceptionhandled/repetitio/DueQueryIT.java
- src/test/java/pl/exceptionhandled/repetitio/PostgresIntegrationTest.java
- src/test/java/pl/exceptionhandled/repetitio/shared/security/JwtValidationTest.java
- src/test/java/pl/exceptionhandled/repetitio/shared/security/RequestSizeFilterTest.java
- src/test/java/pl/exceptionhandled/repetitio/study/domain/SchedulerTest.java
- docs/API.md
- docs/DEVELOPMENT.md
- docs/IMPLEMENTATION_STATUS.md

