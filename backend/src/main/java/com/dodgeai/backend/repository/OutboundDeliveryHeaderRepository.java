package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.OutboundDeliveryHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboundDeliveryHeaderRepository extends JpaRepository<OutboundDeliveryHeader, Long> {
    List<OutboundDeliveryHeader> findByDeliveryDocument(String deliveryDocument);
}