CREATE TABLE evidence (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id),
    url VARCHAR(2048) NOT NULL,
    title VARCHAR(500),
    chunk_text TEXT NOT NULL,
    tavily_score DOUBLE PRECISION,
    embedding TEXT NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_evidence_claim_id ON evidence(claim_id);
