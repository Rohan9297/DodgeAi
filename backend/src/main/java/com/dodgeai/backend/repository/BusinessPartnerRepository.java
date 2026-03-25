package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.BusinessPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessPartnerRepository extends JpaRepository<BusinessPartner, Long> {
    List<BusinessPartner> findByCustomer(String customer);
}