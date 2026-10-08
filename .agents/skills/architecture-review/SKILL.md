---
name: architecture-review
description: Review Repetitio architecture and code changes for coupling, module boundaries, DTO usage, dependency direction and maintainability.
---

# Architecture Review

Review the relevant code, proposed design or git diff from an architectural perspective.

Repetitio should be designed as a modular monolith with clearly separated modules and loosely coupled components.

Prefer ports and adapters when communicating with external technologies or services.

## Check

Pay particular attention to:

- module boundaries
- dependency direction
- coupling between modules
- separation of domain, application and infrastructure concerns
- dependency inversion
- unnecessary dependencies on Spring or infrastructure
- DTO usage at API and module boundaries
- leaking JPA entities outside persistence boundaries
- business logic placed in controllers or repositories
- direct communication between unrelated modules
- abstractions around external systems
- replaceability of infrastructure components
- transactional boundaries
- maintainability and testability

## Architectural principles

Prefer:

Controller
→ DTO
→ Application / Use Case
→ Domain
→ Port
→ Adapter

External technologies such as:

- OpenAI
- vector databases
- embedding providers
- email services
- file storage
- external APIs

should normally be hidden behind interfaces or ports.

Do not introduce abstractions only for the sake of abstraction.

Avoid excessive layering and unnecessary boilerplate.

DTOs should be used mainly at external and module boundaries.

Do not force DTOs between every internal method or domain object.

Domain logic should operate on domain models and value objects where appropriate.

## Review behavior

Do not modify the implementation automatically unless explicitly requested.

Explain architectural problems and their consequences.

When suggesting a change, explain:

- what is wrong
- why it matters
- how you would improve it
- whether the issue should be fixed now or can be postponed

## Output

Return:

1. Architecture summary
2. Critical architectural problems
3. Important improvements
4. Minor improvements
5. Good architectural decisions worth keeping
6. Recommended next actions