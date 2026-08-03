CREATE TABLE claim_verdicts (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE REFERENCES claims(id),
    verdict VARCHAR(20) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    reasoning TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_claim_verdicts_claim_id ON claim_verdicts(claim_id);

ALTER TABLE reels ADD COLUMN trust_score DOUBLE PRECISION;
ALTER TABLE reels ADD COLUMN summary TEXT;
