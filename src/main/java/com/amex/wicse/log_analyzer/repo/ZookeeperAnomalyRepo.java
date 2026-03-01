package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZookeeperAnomalyRepo extends JpaRepository<ZookeeperLogAnomaly, Long> {
    Optional<ZookeeperLogAnomaly> findByAnomalyId(String anomalyId);
}
