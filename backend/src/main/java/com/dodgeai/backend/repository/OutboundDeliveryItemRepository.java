package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.OutboundDeliveryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboundDeliveryItemRepository extends JpaRepository<OutboundDeliveryItem, Long> {
    List<OutboundDeliveryItem> findByDeliveryDocument(String deliveryDocument);

    List<OutboundDeliveryItem> findByReferenceSdDocument(String referenceSdDocument);
}