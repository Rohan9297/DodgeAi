package com.dodgeai.backend.loader;

import com.dodgeai.backend.model.*;
import com.dodgeai.backend.repository.*;
import com.dodgeai.backend.service.GraphService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final SalesOrderHeaderRepository salesOrderHeaderRepo;
    private final SalesOrderItemRepository salesOrderItemRepo;
    private final BillingDocumentHeaderRepository billingHeaderRepo;
    private final BillingDocumentItemRepository billingItemRepo;
    private final OutboundDeliveryHeaderRepository deliveryHeaderRepo;
    private final OutboundDeliveryItemRepository deliveryItemRepo;
    private final PaymentRepository paymentRepo;
    private final JournalEntryRepository journalEntryRepo;
    private final BusinessPartnerRepository businessPartnerRepo;
    private final ProductRepository productRepo;

    // caling graphService
    private final GraphService graphService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadAll() {
        log.info("=== Starting Data Load ===");
        loadSalesOrderHeaders();
        loadSalesOrderItems();
        loadBillingHeaders();
        loadBillingItems();
        loadDeliveryHeaders();
        loadDeliveryItems();
        loadPayments();
        loadJournalEntries();
        loadBusinessPartners();
        loadProducts();
        log.info("=== Data Load Complete ===");

        graphService.buildGraph();
    }

    // ---------- helper ----------
    private java.util.List<JsonNode> readJsonl(String pattern) {
        java.util.List<JsonNode> records = new java.util.ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            records.add(objectMapper.readTree(line));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not load pattern {}: {}", pattern, e.getMessage());
        }
        return records;
    }

    private String get(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return (val == null || val.isNull()) ? null : val.asText();
    }

    // ---------- loaders ----------

    private void loadSalesOrderHeaders() {
        var records = readJsonl("classpath*:data/sales_order_headers/*.jsonl");
        for (JsonNode n : records) {
            salesOrderHeaderRepo.save(SalesOrderHeader.builder()
                    .salesOrder(get(n, "salesOrder"))
                    .salesOrderType(get(n, "salesOrderType"))
                    .salesOrganization(get(n, "salesOrganization"))
                    .distributionChannel(get(n, "distributionChannel"))
                    .soldToParty(get(n, "soldToParty"))
                    .creationDate(get(n, "creationDate"))
                    .totalNetAmount(get(n, "totalNetAmount"))
                    .overallDeliveryStatus(get(n, "overallDeliveryStatus"))
                    .overallOrdReltdBillgStatus(get(n, "overallOrdReltdBillgStatus"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .requestedDeliveryDate(get(n, "requestedDeliveryDate"))
                    .customerPaymentTerms(get(n, "customerPaymentTerms"))
                    .build());
        }
        log.info("Loaded {} sales order headers", records.size());
    }

    private void loadSalesOrderItems() {
        var records = readJsonl("classpath*:data/sales_order_items/*.jsonl");
        for (JsonNode n : records) {
            salesOrderItemRepo.save(SalesOrderItem.builder()
                    .salesOrder(get(n, "salesOrder"))
                    .salesOrderItem(get(n, "salesOrderItem"))
                    .salesOrderItemCategory(get(n, "salesOrderItemCategory"))
                    .material(get(n, "material"))
                    .requestedQuantity(get(n, "requestedQuantity"))
                    .requestedQuantityUnit(get(n, "requestedQuantityUnit"))
                    .netAmount(get(n, "netAmount"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .materialGroup(get(n, "materialGroup"))
                    .productionPlant(get(n, "productionPlant"))
                    .storageLocation(get(n, "storageLocation"))
                    .build());
        }
        log.info("Loaded {} sales order items", records.size());
    }

    private void loadBillingHeaders() {
        var records = readJsonl("classpath*:data/billing_document_headers/*.jsonl");
        for (JsonNode n : records) {
            billingHeaderRepo.save(BillingDocumentHeader.builder()
                    .billingDocument(get(n, "billingDocument"))
                    .billingDocumentType(get(n, "billingDocumentType"))
                    .creationDate(get(n, "creationDate"))
                    .billingDocumentDate(get(n, "billingDocumentDate"))
                    .billingDocumentIsCancelled(get(n, "billingDocumentIsCancelled"))
                    .totalNetAmount(get(n, "totalNetAmount"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .companyCode(get(n, "companyCode"))
                    .fiscalYear(get(n, "fiscalYear"))
                    .accountingDocument(get(n, "accountingDocument"))
                    .soldToParty(get(n, "soldToParty"))
                    .build());
        }
        log.info("Loaded {} billing headers", records.size());
    }

    private void loadBillingItems() {
        var records = readJsonl("classpath*:data/billing_document_items/*.jsonl");
        for (JsonNode n : records) {
            billingItemRepo.save(BillingDocumentItem.builder()
                    .billingDocument(get(n, "billingDocument"))
                    .billingDocumentItem(get(n, "billingDocumentItem"))
                    .material(get(n, "material"))
                    .billingQuantity(get(n, "billingQuantity"))
                    .billingQuantityUnit(get(n, "billingQuantityUnit"))
                    .netAmount(get(n, "netAmount"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .referenceSdDocument(get(n, "referenceSdDocument"))
                    .referenceSdDocumentItem(get(n, "referenceSdDocumentItem"))
                    .build());
        }
        log.info("Loaded {} billing items", records.size());
    }

    private void loadDeliveryHeaders() {
        var records = readJsonl("classpath*:data/outbound_delivery_headers/*.jsonl");
        for (JsonNode n : records) {
            deliveryHeaderRepo.save(OutboundDeliveryHeader.builder()
                    .deliveryDocument(get(n, "deliveryDocument"))
                    .shippingPoint(get(n, "shippingPoint"))
                    .creationDate(get(n, "creationDate"))
                    .actualGoodsMovementDate(get(n, "actualGoodsMovementDate"))
                    .overallGoodsMovementStatus(get(n, "overallGoodsMovementStatus"))
                    .overallPickingStatus(get(n, "overallPickingStatus"))
                    .deliveryBlockReason(get(n, "deliveryBlockReason"))
                    .headerBillingBlockReason(get(n, "headerBillingBlockReason"))
                    .build());
        }
        log.info("Loaded {} delivery headers", records.size());
    }

    private void loadDeliveryItems() {
        var records = readJsonl("classpath*:data/outbound_delivery_items/*.jsonl");
        for (JsonNode n : records) {
            deliveryItemRepo.save(OutboundDeliveryItem.builder()
                    .deliveryDocument(get(n, "deliveryDocument"))
                    .deliveryDocumentItem(get(n, "deliveryDocumentItem"))
                    .referenceSdDocument(get(n, "referenceSdDocument"))
                    .referenceSdDocumentItem(get(n, "referenceSdDocumentItem"))
                    .plant(get(n, "plant"))
                    .storageLocation(get(n, "storageLocation"))
                    .actualDeliveryQuantity(get(n, "actualDeliveryQuantity"))
                    .deliveryQuantityUnit(get(n, "deliveryQuantityUnit"))
                    .build());
        }
        log.info("Loaded {} delivery items", records.size());
    }

    private void loadPayments() {
        var records = readJsonl("classpath*:data/payments_accounts_receivable/*.jsonl");
        for (JsonNode n : records) {
            paymentRepo.save(Payment.builder()
                    .companyCode(get(n, "companyCode"))
                    .fiscalYear(get(n, "fiscalYear"))
                    .accountingDocument(get(n, "accountingDocument"))
                    .accountingDocumentItem(get(n, "accountingDocumentItem"))
                    .customer(get(n, "customer"))
                    .invoiceReference(get(n, "invoiceReference"))
                    .salesDocument(get(n, "salesDocument"))
                    .salesDocumentItem(get(n, "salesDocumentItem"))
                    .amountInTransactionCurrency(get(n, "amountInTransactionCurrency"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .postingDate(get(n, "postingDate"))
                    .clearingDate(get(n, "clearingDate"))
                    .build());
        }
        log.info("Loaded {} payments", records.size());
    }

    private void loadJournalEntries() {
        var records = readJsonl(
                "classpath*:data/journal_entry_items_accounts_receivable/*.jsonl");
        for (JsonNode n : records) {
            journalEntryRepo.save(JournalEntry.builder()
                    .companyCode(get(n, "companyCode"))
                    .fiscalYear(get(n, "fiscalYear"))
                    .accountingDocument(get(n, "accountingDocument"))
                    .accountingDocumentItem(get(n, "accountingDocumentItem"))
                    .customer(get(n, "customer"))
                    .glAccount(get(n, "glAccount"))
                    .referenceDocument(get(n, "referenceDocument"))
                    .amountInTransactionCurrency(get(n, "amountInTransactionCurrency"))
                    .transactionCurrency(get(n, "transactionCurrency"))
                    .postingDate(get(n, "postingDate"))
                    .clearingDate(get(n, "clearingDate"))
                    .accountingDocumentType(get(n, "accountingDocumentType"))
                    .build());
        }
        log.info("Loaded {} journal entries", records.size());
    }

    private void loadBusinessPartners() {
        var records = readJsonl("classpath*:data/business_partners/*.jsonl");
        for (JsonNode n : records) {
            businessPartnerRepo.save(BusinessPartner.builder()
                    .businessPartner(get(n, "businessPartner"))
                    .customer(get(n, "customer"))
                    .businessPartnerFullName(get(n, "businessPartnerFullName"))
                    .businessPartnerName(get(n, "businessPartnerName"))
                    .firstName(get(n, "firstName"))
                    .lastName(get(n, "lastName"))
                    .industry(get(n, "industry"))
                    .businessPartnerCategory(get(n, "businessPartnerCategory"))
                    .creationDate(get(n, "creationDate"))
                    .businessPartnerIsBlocked(get(n, "businessPartnerIsBlocked"))
                    .build());
        }
        log.info("Loaded {} business partners", records.size());
    }

    private void loadProducts() {
        var records = readJsonl("classpath*:data/products/*.jsonl");
        for (JsonNode n : records) {
            productRepo.save(Product.builder()
                    .product(get(n, "product"))
                    .productType(get(n, "productType"))
                    .creationDate(get(n, "creationDate"))
                    .productGroup(get(n, "productGroup"))
                    .baseUnit(get(n, "baseUnit"))
                    .division(get(n, "division"))
                    .industrySector(get(n, "industrySector"))
                    .grossWeight(get(n, "grossWeight"))
                    .netWeight(get(n, "netWeight"))
                    .weightUnit(get(n, "weightUnit"))
                    .crossPlantStatus(get(n, "crossPlantStatus"))
                    .build());
        }
        log.info("Loaded {} products", records.size());
    }
}
