CREATE TABLE session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    user_id UUID NOT NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE session_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    session_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_status_session FOREIGN KEY (session_id) REFERENCES session(id)
);

CREATE INDEX idx_session_user_id ON session(user_id);
CREATE INDEX idx_session_deleted_at ON session(deleted_at);
CREATE INDEX idx_session_status_session_id ON session_status(session_id);
CREATE INDEX idx_session_status_timestamp ON session_status(timestamp);
CREATE INDEX idx_session_status_deleted_at ON session_status(deleted_at);
