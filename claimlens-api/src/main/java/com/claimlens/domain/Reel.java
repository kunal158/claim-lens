package com.claimlens.domain;

import com.claimlens.domain.enums.ReelStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reels")
public class Reel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReelStatus status = ReelStatus.PENDING;

    @Column(name = "source_file_path")
    private String sourceFilePath;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "transcript_segments")
    private String transcriptSegments;

    @Column(name = "onscreen_text_segments")
    private String onscreenTextSegments;

    @Column(name = "trust_score")
    private Double trustScore;

    private String summary;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public ReelStatus getStatus() {
        return status;
    }

    public void setStatus(ReelStatus status) {
        this.status = status;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getTranscriptSegments() {
        return transcriptSegments;
    }

    public void setTranscriptSegments(String transcriptSegments) {
        this.transcriptSegments = transcriptSegments;
    }

    public String getOnscreenTextSegments() {
        return onscreenTextSegments;
    }

    public void setOnscreenTextSegments(String onscreenTextSegments) {
        this.onscreenTextSegments = onscreenTextSegments;
    }

    public Double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Double trustScore) {
        this.trustScore = trustScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
