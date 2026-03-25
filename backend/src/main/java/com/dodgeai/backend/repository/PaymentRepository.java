package com.dodgeai.backend.repository;

import com.dodgeai.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomer(String customer);

    List<Payment> findByInvoiceReference(String invoiceReference);

    List<Payment> findBySalesDocument(String salesDocument);
}