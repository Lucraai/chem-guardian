package com.cnchem.guardian.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cnchem.guardian.domain.StandardClause;

public interface StandardClauseRepository extends JpaRepository<StandardClause, Long> {
}

