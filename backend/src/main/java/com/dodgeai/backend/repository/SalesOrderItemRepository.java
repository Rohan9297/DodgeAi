package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {
    List<SalesOrderItem> findBySalesOrder(String salesOrder);

    List<SalesOrderItem> findByMaterial(String material);
}