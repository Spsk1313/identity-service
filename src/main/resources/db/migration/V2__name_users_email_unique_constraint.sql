ALTER TABLE users
    DROP CONSTRAINT users_email_key;

ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);