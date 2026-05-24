package com.cnchem.guardian.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cnchem.guardian.domain.SafetyScenario;

public interface SafetyScenarioRepository extends JpaRepository<SafetyScenario, Long> {
}

