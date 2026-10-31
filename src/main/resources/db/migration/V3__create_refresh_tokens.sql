CREATE TABLE refresh_tokens (
    id UUID primary key DEFAULT get_random_uuid(),

    user_id UUID NOT NULL,
    family_id UUID NOT NULL,

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,

    used_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_refresh_tokens_user
                            FOREIGN KEY (user_id)
                            REFERENCES users(id)
                            ON DELETE CASCADE,

    CONSTRAINT chk_refresh_token_expiry
                            CHECK ( expires_at > created_at )
);

CREATE INDEX idx_refresh_tokens_family
    ON refresh_tokens(family_id);

CREATE index idx_refresh_tokens_expiry
    ON refresh_tokens(expires_at);