package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HDFSAnomalyRepo extends JpaRepository<HDFSLogAnomaly, Long> {
    Optional<HDFSLogAnomaly> findByAnomalyId(String anomalyId);
}
