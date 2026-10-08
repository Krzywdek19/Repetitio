CREATE TABLE flashcard_schedule (
    id UUID PRIMARY KEY,
    flashcard_id UUID NOT NULL UNIQUE REFERENCES flashcard(id) ON DELETE CASCADE,
    next_review_at TIMESTAMPTZ NOT NULL,
    last_reviewed_at TIMESTAMPTZ,
    interval_days INTEGER NOT NULL CHECK (interval_days BETWEEN 0 AND 3650),
    repetition_count INTEGER NOT NULL CHECK (repetition_count BETWEEN 0 AND 1000000),
    ease_factor DOUBLE PRECISION NOT NULL CHECK (ease_factor BETWEEN 1.3 AND 3.0),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0)
);
CREATE INDEX idx_schedule_due ON flashcard_schedule(next_review_at, flashcard_id);

INSERT INTO flashcard_schedule (id, flashcard_id, next_review_at, interval_days, repetition_count, ease_factor)
SELECT gen_random_uuid(), id, created_at, 0, 0, 2.5 FROM flashcard;
