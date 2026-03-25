package com.dodgeai.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessPartner;
    private String customer;
    private String businessPartnerFullName;
    private String businessPartnerName;
    private String firstName;
    private String lastName;
    private String industry;
    private String businessPartnerCategory;
    private String creationDate;
    private String businessPartnerIsBlocked;
}