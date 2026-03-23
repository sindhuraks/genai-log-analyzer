package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import com.amex.wicse.log_analyzer.model.ZookeeperLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZookeeperAnomalyRepo extends JpaRepository<ZookeeperLogAnomaly, Long> {
    Optional<ZookeeperLogAnomaly> findByAnomalyId(String anomalyId);
    @Query(value = """
             SELECT a.*
                            FROM zookeeper_anomalies a
                            WHERE a.id IN (
                                SELECT MAX(id)
                                FROM zookeeper_anomalies
                                GROUP BY level, content
                            )
                            ORDER BY a.time DESC
                            LIMIT 15
            """, nativeQuery = true)
    List<ZookeeperLogAnomaly> listAnomalies();
}
