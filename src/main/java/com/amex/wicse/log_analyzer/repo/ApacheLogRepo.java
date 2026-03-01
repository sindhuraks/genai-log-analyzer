package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApacheLogRepo extends JpaRepository<ApacheLog, Long> {
    List<ApacheLog> findByLevelIn(List<String> levels);
}
