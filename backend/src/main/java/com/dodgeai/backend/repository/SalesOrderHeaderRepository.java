package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.SalesOrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderHeaderRepository extends JpaRepository<SalesOrderHeader, Long> {
    List<SalesOrderHeader> findBySoldToParty(String soldToParty);

    List<SalesOrderHeader> findBySalesOrder(String salesOrder);
}