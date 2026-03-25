package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private String fiscalYear;
    private String accountingDocument;
    private String accountingDocumentItem;
    private String customer;
    private String glAccount;
    private String referenceDocument;
    private String amountInTransactionCurrency;
    private String transactionCurrency;
    private String postingDate;
    private String clearingDate;
    private String accountingDocumentType;
}