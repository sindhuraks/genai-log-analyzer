package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApacheAnomalyRepo extends JpaRepository<ApacheLogAnomaly, Long> {
    Optional<ApacheLogAnomaly> findByAnomalyId(String anomalyId);
}
