CREATE TABLE session_metric (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    session_id UUID NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_session_metric_session FOREIGN KEY (session_id) REFERENCES session(id)
);

CREATE INDEX idx_session_metric_session_id ON session_metric(session_id);
CREATE INDEX idx_session_metric_timestamp ON session_metric(timestamp);
CREATE INDEX idx_session_metric_deleted_at ON session_metric(deleted_at);
