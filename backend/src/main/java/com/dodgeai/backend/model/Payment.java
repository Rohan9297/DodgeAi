package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private String fiscalYear;
    private String accountingDocument;
    private String accountingDocumentItem;
    private String customer;
    private String invoiceReference;
    private String salesDocument;
    private String salesDocumentItem;
    private String amountInTransactionCurrency;
    private String transactionCurrency;
    private String postingDate;
    private String clearingDate;
}