package com.claimlens.repository;

import com.claimlens.domain.EvalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EvalResultRepository extends JpaRepository<EvalResult, Long> {

    List<EvalResult> findByRunType(String runType);

    List<EvalResult> findByRunTypeAndClaimIdIn(String runType, List<Long> claimIds);

    @Transactional
    void deleteByRunTypeAndClaimIdIn(String runType, List<Long> claimIds);
}
