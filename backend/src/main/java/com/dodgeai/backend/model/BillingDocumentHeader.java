package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "billing_document_headers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingDocumentHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billingDocument;
    private String billingDocumentType;
    private String creationDate;
    private String billingDocumentDate;
    private String billingDocumentIsCancelled;
    private String totalNetAmount;
    private String transactionCurrency;
    private String companyCode;
    private String fiscalYear;
    private String accountingDocument;
    private String soldToParty;
}