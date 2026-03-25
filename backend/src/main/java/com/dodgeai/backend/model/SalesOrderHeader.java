package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sales_order_headers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String salesOrder;
    private String salesOrderType;
    private String salesOrganization;
    private String distributionChannel;
    private String soldToParty;
    private String creationDate;
    private String totalNetAmount;
    private String overallDeliveryStatus;
    private String overallOrdReltdBillgStatus;
    private String transactionCurrency;
    private String requestedDeliveryDate;
    private String customerPaymentTerms;
}
