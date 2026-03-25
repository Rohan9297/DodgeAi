package com.dodgeai.backend.service;

import com.dodgeai.backend.model.*;
import com.dodgeai.backend.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphService {

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

    private Graph<GraphNode, LabeledEdge> graph;

    // ── inner classes ──────────────────────────────────────────────

    public static class GraphNode {
        public String id;
        public String label;
        public String type;
        public Map<String, String> properties = new LinkedHashMap<>();

        public GraphNode(String id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }
    }

    public static class LabeledEdge extends DefaultEdge {
        private final String label;

        public LabeledEdge(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // ── build graph after DataLoader finishes ─────────────────────

    public void buildGraph() {
        graph = new SimpleDirectedGraph<>(null, null, false);
        log.info("Building graph...");

        addCustomerNodes();
        addSalesOrderNodes();
        addDeliveryNodes();
        addBillingNodes();
        addPaymentNodes();
        addProductNodes();

        log.info("Graph built: {} nodes, {} edges",
                graph.vertexSet().size(), graph.edgeSet().size());
    }

    // ── node builders ─────────────────────────────────────────────

    private void addCustomerNodes() {
        for (BusinessPartner bp : businessPartnerRepo.findAll()) {
            String id = "CUSTOMER_" + bp.getCustomer();
            GraphNode node = new GraphNode(id,
                    bp.getBusinessPartnerFullName() != null
                            ? bp.getBusinessPartnerFullName()
                            : bp.getCustomer(),
                    "Customer");
            node.properties.put("customerId", bp.getCustomer());
            node.properties.put("name", bp.getBusinessPartnerFullName());
            node.properties.put("industry", bp.getIndustry());
            node.properties.put("blocked", bp.getBusinessPartnerIsBlocked());
            safeAddVertex(node);
        }
    }

    private void addSalesOrderNodes() {
        for (SalesOrderHeader so : salesOrderHeaderRepo.findAll()) {
            String id = "SO_" + so.getSalesOrder();
            GraphNode node = new GraphNode(id, "SO " + so.getSalesOrder(), "SalesOrder");
            node.properties.put("salesOrder", so.getSalesOrder());
            node.properties.put("type", so.getSalesOrderType());
            node.properties.put("amount", so.getTotalNetAmount());
            node.properties.put("currency", so.getTransactionCurrency());
            node.properties.put("deliveryStatus", so.getOverallDeliveryStatus());
            node.properties.put("billingStatus", so.getOverallOrdReltdBillgStatus());
            node.properties.put("creationDate", so.getCreationDate());
            safeAddVertex(node);

            // Edge: Customer → SalesOrder
            if (so.getSoldToParty() != null) {
                GraphNode customerNode = findNode("CUSTOMER_" + so.getSoldToParty());
                if (customerNode != null) {
                    safeAddEdge(customerNode, node, "PLACED");
                }
            }

            // Edges: SalesOrder → SalesOrderItems
            for (SalesOrderItem item : salesOrderItemRepo.findBySalesOrder(so.getSalesOrder())) {
                String itemId = "SOI_" + so.getSalesOrder() + "_" + item.getSalesOrderItem();
                GraphNode itemNode = new GraphNode(itemId,
                        "Item " + item.getSalesOrderItem(), "SalesOrderItem");
                itemNode.properties.put("salesOrder", item.getSalesOrder());
                itemNode.properties.put("item", item.getSalesOrderItem());
                itemNode.properties.put("material", item.getMaterial());
                itemNode.properties.put("quantity", item.getRequestedQuantity());
                itemNode.properties.put("netAmount", item.getNetAmount());
                safeAddVertex(itemNode);
                safeAddEdge(node, itemNode, "HAS_ITEM");

                // Edge: SalesOrderItem → Product
                if (item.getMaterial() != null) {
                    GraphNode productNode = findNode("PRODUCT_" + item.getMaterial());
                    if (productNode != null) {
                        safeAddEdge(itemNode, productNode, "REFERS_TO");
                    }
                }
            }
        }
    }

    private void addDeliveryNodes() {
        for (OutboundDeliveryHeader dh : deliveryHeaderRepo.findAll()) {
            String id = "DEL_" + dh.getDeliveryDocument();
            GraphNode node = new GraphNode(id,
                    "Delivery " + dh.getDeliveryDocument(), "Delivery");
            node.properties.put("deliveryDocument", dh.getDeliveryDocument());
            node.properties.put("shippingPoint", dh.getShippingPoint());
            node.properties.put("goodsMovementDate", dh.getActualGoodsMovementDate());
            node.properties.put("goodsMovementStatus", dh.getOverallGoodsMovementStatus());
            safeAddVertex(node);

            // Edges: DeliveryItem → SalesOrder (via referenceSdDocument)
            for (OutboundDeliveryItem item : deliveryItemRepo.findByDeliveryDocument(dh.getDeliveryDocument())) {
                if (item.getReferenceSdDocument() != null) {
                    GraphNode soNode = findNode("SO_" + item.getReferenceSdDocument());
                    if (soNode != null) {
                        safeAddEdge(soNode, node, "DELIVERED_BY");
                    }
                }
            }
        }
    }

    private void addBillingNodes() {
        for (BillingDocumentHeader bh : billingHeaderRepo.findAll()) {
            String id = "BILL_" + bh.getBillingDocument();
            GraphNode node = new GraphNode(id,
                    "Invoice " + bh.getBillingDocument(), "Billing");
            node.properties.put("billingDocument", bh.getBillingDocument());
            node.properties.put("type", bh.getBillingDocumentType());
            node.properties.put("amount", bh.getTotalNetAmount());
            node.properties.put("currency", bh.getTransactionCurrency());
            node.properties.put("date", bh.getBillingDocumentDate());
            node.properties.put("cancelled", bh.getBillingDocumentIsCancelled());
            safeAddVertex(node);

            // Edge: SalesOrder → Billing (via billing items referenceSdDocument)
            for (BillingDocumentItem item : billingItemRepo.findByBillingDocument(bh.getBillingDocument())) {
                if (item.getReferenceSdDocument() != null) {
                    GraphNode soNode = findNode("SO_" + item.getReferenceSdDocument());
                    if (soNode != null) {
                        safeAddEdge(soNode, node, "BILLED_BY");
                    }
                }
            }
        }
    }

    private void addPaymentNodes() {
        for (Payment p : paymentRepo.findAll()) {
            String id = "PAY_" + p.getAccountingDocument() + "_" + p.getAccountingDocumentItem();
            GraphNode node = new GraphNode(id,
                    "Payment " + p.getAccountingDocument(), "Payment");
            node.properties.put("accountingDocument", p.getAccountingDocument());
            node.properties.put("customer", p.getCustomer());
            node.properties.put("amount", p.getAmountInTransactionCurrency());
            node.properties.put("currency", p.getTransactionCurrency());
            node.properties.put("postingDate", p.getPostingDate());
            node.properties.put("clearingDate", p.getClearingDate());
            safeAddVertex(node);

            // Edge: Billing → Payment
            if (p.getInvoiceReference() != null) {
                GraphNode billNode = findNode("BILL_" + p.getInvoiceReference());
                if (billNode != null) {
                    safeAddEdge(billNode, node, "PAID_BY");
                }
            }
        }
    }

    private void addProductNodes() {
        for (Product p : productRepo.findAll()) {
            String id = "PRODUCT_" + p.getProduct();
            GraphNode node = new GraphNode(id, "Product " + p.getProduct(), "Product");
            node.properties.put("product", p.getProduct());
            node.properties.put("type", p.getProductType());
            node.properties.put("group", p.getProductGroup());
            node.properties.put("division", p.getDivision());
            node.properties.put("grossWeight", p.getGrossWeight());
            safeAddVertex(node);
        }
    }

    // ── helpers ───────────────────────────────────────────────────

    private void safeAddVertex(GraphNode node) {
        if (findNode(node.id) == null) {
            graph.addVertex(node);
        }
    }

    private void safeAddEdge(GraphNode source, GraphNode target, String label) {
        try {
            if (!graph.containsEdge(source, target)) {
                graph.addEdge(source, target, new LabeledEdge(label));
            }
        } catch (Exception e) {
            log.debug("Could not add edge {}->{}: {}", source.id, target.id, e.getMessage());
        }
    }

    private GraphNode findNode(String id) {
        return graph.vertexSet().stream()
                .filter(n -> n.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    // ── public API used by controllers ────────────────────────────

    public Map<String, Object> getGraphData() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (GraphNode node : graph.vertexSet()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", node.id);
            data.put("label", node.label);
            data.put("type", node.type);
            data.putAll(node.properties);
            nodes.add(Map.of("data", data));
        }

        for (LabeledEdge edge : graph.edgeSet()) {
            GraphNode src = graph.getEdgeSource(edge);
            GraphNode tgt = graph.getEdgeTarget(edge);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", src.id + "_" + tgt.id);
            data.put("source", src.id);
            data.put("target", tgt.id);
            data.put("label", edge.getLabel());
            edges.add(Map.of("data", data));
        }

        return Map.of("nodes", nodes, "edges", edges);
    }

    public GraphNode getNodeById(String id) {
        return findNode(id);
    }

    public List<Map<String, Object>> getNeighbors(String nodeId) {
        GraphNode node = findNode(nodeId);
        if (node == null)
            return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (LabeledEdge edge : graph.edgesOf(node)) {
            GraphNode neighbor = graph.getEdgeSource(edge).id.equals(nodeId)
                    ? graph.getEdgeTarget(edge)
                    : graph.getEdgeSource(edge);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", neighbor.id);
            info.put("label", neighbor.label);
            info.put("type", neighbor.type);
            info.put("relationship", edge.getLabel());
            result.add(info);
        }
        return result;
    }

    public Graph<GraphNode, LabeledEdge> getGraph() {
        return graph;
    }
}
