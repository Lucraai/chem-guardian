package com.cnchem.guardian.repository;

import com.cnchem.guardian.domain.StandardLibraryVersionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardLibraryVersionHistoryRepository extends JpaRepository<StandardLibraryVersionHistory, Long> {
    List<StandardLibraryVersionHistory> findByEntryIdOrderByVersionNoDescCreatedAtDesc(Long entryId);
}
