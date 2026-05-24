package com.cnchem.guardian.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cnchem.guardian.domain.AdvisorQuestion;

public interface AdvisorQuestionRepository extends JpaRepository<AdvisorQuestion, Long> {
}

