package com.claimlens.domain;

import com.claimlens.domain.enums.ClaimSource;
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
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reel_id", nullable = false)
    private Long reelId;

    @Column(name = "claim_text", nullable = false)
    private String claimText;

    @Column(name = "search_query")
    private String searchQuery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimSource source;

    @Column(name = "start_s", nullable = false)
    private double startS;

    @Column(name = "end_s", nullable = false)
    private double endS;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public Long getReelId() {
        return reelId;
    }

    public void setReelId(Long reelId) {
        this.reelId = reelId;
    }

    public String getClaimText() {
        return claimText;
    }

    public void setClaimText(String claimText) {
        this.claimText = claimText;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public ClaimSource getSource() {
        return source;
    }

    public void setSource(ClaimSource source) {
        this.source = source;
    }

    public double getStartS() {
        return startS;
    }

    public void setStartS(double startS) {
        this.startS = startS;
    }

    public double getEndS() {
        return endS;
    }

    public void setEndS(double endS) {
        this.endS = endS;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
