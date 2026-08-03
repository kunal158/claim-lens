CREATE TABLE claims (
    id BIGSERIAL PRIMARY KEY,
    reel_id BIGINT NOT NULL REFERENCES reels(id),
    claim_text TEXT NOT NULL,
    source VARCHAR(20) NOT NULL,
    start_s DOUBLE PRECISION NOT NULL,
    end_s DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_claims_reel_id ON claims(reel_id);
