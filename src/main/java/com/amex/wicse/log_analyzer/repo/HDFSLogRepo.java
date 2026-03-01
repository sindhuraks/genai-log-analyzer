package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.ApacheLog;
import com.amex.wicse.log_analyzer.model.HDFSLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HDFSLogRepo extends JpaRepository<HDFSLog, Long> {
    List<HDFSLog> findByLevelIn(List<String> levels);
}
