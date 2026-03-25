package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "billing_document_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingDocumentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billingDocument;
    private String billingDocumentItem;
    private String material;
    private String billingQuantity;
    private String billingQuantityUnit;
    private String netAmount;
    private String transactionCurrency;
    private String referenceSdDocument;
    private String referenceSdDocumentItem;
}