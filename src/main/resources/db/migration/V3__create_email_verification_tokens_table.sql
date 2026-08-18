CREATE TABLE email_verification_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_email_verification_token_hash
        UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE UNIQUE INDEX uq_email_verification_tokens_outstanding_user
ON email_verification_tokens(user_id)
WHERE used_at IS NULL
AND invalidated_at IS NULL;