package com.claimlens.repository;

import com.claimlens.domain.Reel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReelRepository extends JpaRepository<Reel, Long> {

    Optional<Reel> findBySourceUrl(String sourceUrl);

    List<Reel> findAllByOrderByCreatedAtDesc();
}
