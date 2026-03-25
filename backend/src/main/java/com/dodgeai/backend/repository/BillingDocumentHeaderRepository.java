package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.BillingDocumentHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillingDocumentHeaderRepository extends JpaRepository<BillingDocumentHeader, Long> {
    List<BillingDocumentHeader> findBySoldToParty(String soldToParty);

    List<BillingDocumentHeader> findByBillingDocument(String billingDocument);
}