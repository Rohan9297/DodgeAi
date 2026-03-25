package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;
    private String productType;
    private String creationDate;
    private String productGroup;
    private String baseUnit;
    private String division;
    private String industrySector;
    private String grossWeight;
    private String netWeight;
    private String weightUnit;
    private String crossPlantStatus;
}