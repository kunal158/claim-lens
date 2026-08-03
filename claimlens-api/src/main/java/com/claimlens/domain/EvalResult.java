package com.claimlens.domain;

import com.claimlens.domain.enums.Verdict;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "eval_results")
public class EvalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_id", nullable = false)
    private Long claimId;

    @Column(name = "run_type", nullable = false)
    private String runType;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_verdict", nullable = false)
    private Verdict modelVerdict;

    @Column(name = "model_reasoning", nullable = false)
    private String modelReasoning;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public Verdict getModelVerdict() {
        return modelVerdict;
    }

    public void setModelVerdict(Verdict modelVerdict) {
        this.modelVerdict = modelVerdict;
    }

    public String getModelReasoning() {
        return modelReasoning;
    }

    public void setModelReasoning(String modelReasoning) {
        this.modelReasoning = modelReasoning;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
