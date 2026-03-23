package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApacheAnomalyRepo extends JpaRepository<ApacheLogAnomaly, Long> {
    Optional<ApacheLogAnomaly> findByAnomalyId(String anomalyId);
    @Query(value = """
            SELECT a.*
                            FROM apache_anomalies a
                            WHERE a.id IN (
                                SELECT MAX(id)
                                FROM apache_anomalies
                                GROUP BY level, content
                            )
                            ORDER BY a.time DESC
                            LIMIT 15
            """, nativeQuery = true)
    List<ApacheLogAnomaly> listAnomalies();
}
