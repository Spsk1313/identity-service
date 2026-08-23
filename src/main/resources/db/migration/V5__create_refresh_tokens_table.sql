CREATE TABLE refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_refresh_tokens_token_hash
                            UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_session
                            FOREIGN KEY (session_id)
                            REFERENCES auth_sessions(id)
);

CREATE INDEX idx_refresh_tokens_session_id
ON refresh_tokens(session_id);