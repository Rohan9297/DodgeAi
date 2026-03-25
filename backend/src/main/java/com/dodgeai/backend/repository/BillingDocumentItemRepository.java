package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.BillingDocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BillingDocumentItemRepository extends JpaRepository<BillingDocumentItem, Long> {
    List<BillingDocumentItem> findByBillingDocument(String billingDocument);

    List<BillingDocumentItem> findByReferenceSdDocument(String referenceSdDocument);
}