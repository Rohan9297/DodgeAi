package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outbound_delivery_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundDeliveryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deliveryDocument;
    private String deliveryDocumentItem;
    private String referenceSdDocument;
    private String referenceSdDocumentItem;
    private String plant;
    private String storageLocation;
    private String actualDeliveryQuantity;
    private String deliveryQuantityUnit;
}