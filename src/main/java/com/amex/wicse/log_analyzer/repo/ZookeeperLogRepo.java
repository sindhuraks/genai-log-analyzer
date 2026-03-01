package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.HDFSLog;
import com.amex.wicse.log_analyzer.model.ZookeeperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZookeeperLogRepo extends JpaRepository<ZookeeperLog, Long> {
    List<ZookeeperLog> findByLevelIn(List<String> levels);

}
