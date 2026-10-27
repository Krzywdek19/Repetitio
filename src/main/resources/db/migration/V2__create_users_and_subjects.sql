CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       email VARCHAR(320) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_users_email_not_blank
                           CHECK (BTRIM(email) <> '')
);

CREATE UNIQUE INDEX uq_users_email
    ON users (LOWER(BTRIM(email)));


CREATE TABLE subjects (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          user_id UUID NOT NULL,
                          name VARCHAR(120) NOT NULL,
                          description TEXT,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          CONSTRAINT fk_subjects_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_subjects_name_not_blank
                              CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX uq_subjects_user_name
    ON subjects (user_id, LOWER(BTRIM(name)));