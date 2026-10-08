---
name: security-review
description: Review Repetitio code changes for security vulnerabilities and authorization problems.
---

# Security Review

Review the relevant implementation or diff.

Check especially:

- authentication
- authorization
- resource ownership
- input validation
- SQL/JPA usage
- secrets
- file uploads
- injection attacks
- AI prompt injection
- cross-user data leakage
- RAG document isolation

Do not modify code automatically.

Return:

1. Critical problems
2. Important problems
3. Minor improvements
4. Suggested fixes