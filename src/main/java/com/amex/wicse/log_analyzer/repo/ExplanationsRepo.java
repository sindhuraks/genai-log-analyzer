package com.amex.wicse.log_analyzer.repo;

import com.amex.wicse.log_analyzer.model.Explanations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExplanationsRepo extends JpaRepository<Explanations, Long> {
    Optional<Explanations> findByAnomalyIdAndModel(String anomalyId, String model);
}
