package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByCustomer(String customer);

    List<JournalEntry> findByReferenceDocument(String referenceDocument);
}