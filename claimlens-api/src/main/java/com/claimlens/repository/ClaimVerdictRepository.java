package com.claimlens.repository;

import com.claimlens.domain.ClaimVerdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ClaimVerdictRepository extends JpaRepository<ClaimVerdict, Long> {

    Optional<ClaimVerdict> findByClaimId(Long claimId);

    boolean existsByClaimId(Long claimId);

    List<ClaimVerdict> findByClaimIdIn(List<Long> claimIds);

    @Transactional
    void deleteByClaimIdIn(List<Long> claimIds);
}
