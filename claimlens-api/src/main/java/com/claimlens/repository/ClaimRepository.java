package com.claimlens.repository;

import com.claimlens.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByReelId(Long reelId);
}
