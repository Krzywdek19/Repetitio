---
name: implementation-review
description: Review completed Repetitio code changes before acceptance. Check correctness, tests, edge cases, maintainability and consistency with project conventions.
---

# Implementation Review

Review the implementation, relevant files or current git diff.

Act as a code reviewer.

The goal is to help the project owner understand and evaluate the implementation before accepting it.

Do not automatically modify code unless explicitly requested.

## Check correctness

Look for:

- logical bugs
- incorrect assumptions
- null handling
- edge cases
- race conditions
- transaction problems
- incorrect exception handling
- invalid state transitions
- potential data consistency problems

## Check Java and Spring usage

Pay attention to:

- Spring dependency injection
- transaction boundaries
- JPA usage
- lazy loading problems
- query efficiency
- validation
- exception mapping
- immutability where useful
- appropriate collection usage
- thread safety when relevant
- unnecessary framework complexity

## Check tests

Evaluate whether the change has appropriate tests.

Consider:

- unit tests
- integration tests
- repository tests
- controller tests
- concurrency tests when relevant
- failure paths
- boundary conditions

Do not recommend tests merely to increase coverage.

Tests should verify meaningful behavior.

## Check maintainability

Look for:

- duplicated logic
- overly large classes or methods
- unclear naming
- hidden side effects
- unnecessary complexity
- violations of project architecture
- tight coupling
- unclear responsibilities

## Review behavior

Be critical but practical.

Do not request large refactors unless they provide meaningful value.

Distinguish between:

- must fix
- should fix
- optional improvement

Explain important findings so that the project owner can learn from them.

## Output

Return:

1. What the implementation does
2. Must-fix issues
3. Should-fix issues
4. Optional improvements
5. Test assessment
6. What was done well
7. Final recommendation:
    - approve
    - approve after minor changes
    - request changes