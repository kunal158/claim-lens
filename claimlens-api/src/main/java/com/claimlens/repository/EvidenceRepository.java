package com.claimlens.repository;

import com.claimlens.domain.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    List<Evidence> findByClaimIdOrderBySimilarityScoreDesc(Long claimId);

    boolean existsByClaimId(Long claimId);

    @Transactional
    void deleteByClaimIdIn(List<Long> claimIds);
}
