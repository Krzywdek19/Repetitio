# Repetitio

Repetitio is a learning platform using notes, flashcards,
quizzes and retrieval-augmented generation.

## Working style

- Before implementing a non-trivial feature, present a short plan.
- Do not make large architectural changes without approval.
- Prefer small, reviewable changes.
- After implementation, summarize changed files.
- Run relevant tests before considering work complete.
- Do not create git commits unless explicitly requested.

## Architecture

- Use a modular monolith.
- Keep modules loosely coupled.
- Prefer ports and adapters where external technology is involved.
- Do not expose JPA entities outside persistence/application boundaries.
- Use DTOs at API and module boundaries.
- Domain logic should not depend on Spring infrastructure.

## RAG

- The project owner wants to implement and understand the RAG system personally.
- Do not implement complete RAG workflows unless explicitly requested.
- Prefer explaining approaches, reviewing code and proposing interfaces.
- Highlight security and data-isolation risks.

## Security

- Treat security as a first-class requirement.
- Validate all external input.
- Verify authorization at resource level.
- Never commit secrets.
- Pay special attention to uploaded documents and AI prompt injection.