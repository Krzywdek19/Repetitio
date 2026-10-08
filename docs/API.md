# MVP API

All request/response bodies use JSON. Resource IDs are UUIDs and timestamps are UTC instants.
Supply access tokens using `Authorization: Bearer <accessToken>`.
Only the two authentication POST endpoints are public.

## Authentication

| Method | Path | Request | Result |
| --- | --- | --- | --- |
| POST | /api/auth/register | email, password | 201: id, email, createdAt |
| POST | /api/auth/login | email, password | 200: accessToken, tokenType, expiresIn |

Emails are stripped and lowercased using Locale.ROOT. Normalized emails are unique.
Duplicate registration returns 409, including concurrent registration.
Passwords are not trimmed; they must contain 12–72 UTF-16 code units and at most 72 UTF-8 bytes.
BCrypt uses cost 12. Unknown accounts and incorrect passwords share the same 401 response.
Passwords, hashes, and tokens must not be logged.

Tokens use RS256 and expire after 900 seconds. They include sub (user UUID), iss, aud,
iat, exp, and jti. Validation allows 30 seconds of clock skew. There is no refresh token
or immediate revocation mechanism. A jti does not prevent replay.

## Notes and flashcards

| Method | Path | Request / behavior |
| --- | --- | --- |
| POST | /api/notes | title, content; returns 201 |
| GET | /api/notes | paginated owned notes |
| GET | /api/notes/{id} | owned note |
| PATCH | /api/notes/{id} | optional title, content |
| DELETE | /api/notes/{id} | returns 204 |
| POST | /api/flashcards | front, back, optional noteId; returns 201 |
| GET | /api/flashcards | paginated owned cards |
| GET | /api/flashcards/{id} | owned card |
| PATCH | /api/flashcards/{id} | optional front, back, noteId |
| DELETE | /api/flashcards/{id} | returns 204 |

Titles have a 200-character limit, note content 50,000, and card front/back 5,000.
Text fields must not be blank. Bodies have a 256 KiB limit, including chunked bodies.
Content is plain text; a future browser client must render it safely.
Unknown input properties are rejected. The server derives ownership from the authenticated principal.

PATCH preserves omitted fields. Explicit null text values are invalid.
For cards, `"noteId": null` detaches the note. A non-null note ID must belong to the caller.
An empty PATCH is allowed and updates the modification timestamp.

Nonexistent resources and resources owned by another user both return 404.
Deleting a note detaches its cards; deleting a card cascades to its schedule.
Ordinary note/card edits use last-write-wins; review concurrency has stricter semantics.

Lists accept `page` (0–100000, default 0) and `size` (1–100, default 20).
Responses contain `items`, `page`, `size`, and `hasNext`.
Notes/cards are ordered by createdAt descending, then ID. Client-defined sorting is not supported.

## Study

`GET /api/study/due?page=0&size=20` returns owned cards with nextReviewAt <= server time.
Items contain flashcardId, front, back, nextReviewAt, and version.
Ordering is nextReviewAt ascending, then card ID. Ownership is applied before pagination.

`POST /api/study/flashcards/{id}/review`:

```json
{"rating": "GOOD", "expectedVersion": 0}
```

Ratings: AGAIN, HARD, GOOD, EASY. expectedVersion is required and nonnegative.
The response contains id (schedule UUID), flashcardId, state, and the new version.
State contains nextReviewAt, lastReviewedAt, intervalDays, repetitionCount, and easeFactor.

Reload after 409. A stale client version, simultaneous update, or a card not yet due returns 409.
The server never retries a failed review automatically. Successful review is atomic.
New cards receive an immediately due schedule in the same transaction as card creation.

### Initial scheduling rules

This deliberately simple algorithm is not FSRS or an implementation of SM-2.

- Initial state: interval 0, repetitions 0, ease 2.5.
- AGAIN: interval 1 day, repetitions reset to 0, ease reduced by 0.2.
- First successful review (also after AGAIN): HARD 1 day, GOOD 2 days, EASY 4 days.
- Later successful review: HARD multiplies interval by 1.2; GOOD by updated ease;
  EASY by updated ease × 1.3. Round to nearest integer and increase by at least one day.
- HARD reduces ease by 0.15; EASY increases it by 0.15.
- Ease is bounded to 1.3–3.0, interval to 1–3650 days, and repetitions to 1,000,000.
- Next review is measured from server review time. A day is 24 hours.
- Same-session relearning is not supported.

## Errors

Errors use problem-details JSON. Common statuses are 400 (validation), 401 (authentication),
403 (forbidden), 404 (unavailable resource), 409 (conflict), and 413 (oversized body).
Responses do not include passwords, hashes, SQL, or stack traces.
