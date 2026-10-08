CREATE TABLE note (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user(id),
    title VARCHAR(200) NOT NULL,
    content VARCHAR(50000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_note_owner_created ON note(owner_id, created_at DESC, id);

CREATE TABLE flashcard (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES app_user(id),
    note_id UUID REFERENCES note(id) ON DELETE SET NULL,
    front VARCHAR(5000) NOT NULL,
    back VARCHAR(5000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_flashcard_owner_created ON flashcard(owner_id, created_at DESC, id);
CREATE INDEX idx_flashcard_note ON flashcard(note_id);
