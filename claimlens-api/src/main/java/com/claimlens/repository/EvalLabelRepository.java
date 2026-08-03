package com.claimlens.repository;

import com.claimlens.domain.EvalLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvalLabelRepository extends JpaRepository<EvalLabel, Long> {

    Optional<EvalLabel> findByClaimId(Long claimId);
}
