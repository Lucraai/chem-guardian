package com.cnchem.guardian.repository;

import com.cnchem.guardian.domain.StandardLibraryEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardLibraryEntryRepository extends JpaRepository<StandardLibraryEntry, Long> {
    List<StandardLibraryEntry> findAllByOrderByCodeAsc();
    Optional<StandardLibraryEntry> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
