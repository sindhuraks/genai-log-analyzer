package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLogAnomaly;
import com.amex.wicse.log_analyzer.model.HDFSLogAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HDFSAnomalyRepo extends JpaRepository<HDFSLogAnomaly, Long> {
    Optional<HDFSLogAnomaly> findByAnomalyId(String anomalyId);
//    @Query(value = """
//            SELECT a.*
//                            FROM hdfs_anomalies a
//                            WHERE a.id IN (
//                                SELECT MAX(id)
//                                FROM hdfs_anomalies
//                                GROUP BY level, content
//                            )
//                            ORDER BY a.time DESC
//                            LIMIT 5
//            """, nativeQuery = true)
    @Query(value = """
            SELECT DISTINCT ON (regexp_replace(content, '\\d+', 'N', 'g')) *
            FROM hdfs_anomalies 
            ORDER BY regexp_replace(content, '\\d+', 'N', 'g'), time;
            """, nativeQuery = true)
    List<HDFSLogAnomaly> listAnomalies();
}
