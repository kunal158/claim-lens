CREATE TABLE eval_labels (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL UNIQUE REFERENCES claims(id),
    human_label VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE eval_results (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL REFERENCES claims(id),
    run_type VARCHAR(30) NOT NULL,
    model_verdict VARCHAR(20) NOT NULL,
    model_reasoning TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_eval_results_run_type ON eval_results(run_type);
