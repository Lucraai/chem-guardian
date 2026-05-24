package com.cnchem.guardian.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cnchem.guardian.domain.StandardDocument;

public interface StandardDocumentRepository extends JpaRepository<StandardDocument, Long> {
}

