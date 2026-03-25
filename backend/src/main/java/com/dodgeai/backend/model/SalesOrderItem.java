package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sales_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String salesOrder;
    private String salesOrderItem;
    private String salesOrderItemCategory;
    private String material;
    private String requestedQuantity;
    private String requestedQuantityUnit;
    private String netAmount;
    private String transactionCurrency;
    private String materialGroup;
    private String productionPlant;
    private String storageLocation;
}