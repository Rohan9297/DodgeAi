package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outbound_delivery_headers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundDeliveryHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deliveryDocument;
    private String shippingPoint;
    private String creationDate;
    private String actualGoodsMovementDate;
    private String overallGoodsMovementStatus;
    private String overallPickingStatus;
    private String deliveryBlockReason;
    private String headerBillingBlockReason;
}