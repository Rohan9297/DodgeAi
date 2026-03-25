package com.dodgeai.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LLMService {

   @Value("${gemini.api.key}")
   private String apiKey;

   @Value("${gemini.api.url}")
   private String apiUrl;

   private final RestTemplate restTemplate = new RestTemplate();
   private final ObjectMapper objectMapper = new ObjectMapper();

   private static final String SCHEMA = """
                                   You have access to these H2 database tables:

            IMPORTANT: Table names are LOWERCASE, column names are UPPERCASE.
            Always write table names exactly as shown below (lowercase with underscores).
            Always write column names in UPPERCASE.

         1. sales_order_headers: id, salesorder, salesordertype, salesorganization,
              distributionchannel, soldtoparty, creationdate, totalnetamount,
              overalldeliverystatus, overallordreltdbillgstatus, transactioncurrency,
              requesteddeliverydate, customerpaymentterms

           2. sales_order_items: id, salesorder, salesorderitem, salesorderitemcategory,
              material, requestedquantity, requestedquantityunit, netamount,
              transactioncurrency, materialgroup, productionplant, storagelocation

           3. billing_document_headers: id, billingdocument, billingdocumenttype,
              creationdate, billingdocumentdate, billingdocumentiscancelled,
              totalnetamount, transactioncurrency, companycode, fiscalyear,
              accountingdocument, soldtoparty

           4. billing_document_items: id, billingdocument, billingdocumentitem,
              material, billingquantity, billingquantityunit, netamount,
              transactioncurrency, referencesddocument, referencesddocumentitem

           5. outbound_delivery_headers: id, deliverydocument, shippingpoint,
              creationdate, actualgoodsmovementdate, overallgoodsmovementstatus,
              overallpickingstatus, deliveryblockreason, headerbillingblockreason

           6. outbound_delivery_items: id, deliverydocument, deliverydocumentitem,
              referencesddocument, referencesddocumentitem, plant, storagelocation,
              actualdeliveryquantity, deliveryquantityunit

           7. payments: id, companycode, fiscalyear, accountingdocument,
              accountingdocumentitem, customer, invoicereference, salesdocument,
              salesdocumentitem, amountintransactioncurrency, transactioncurrency,
              postingdate, clearingdate

           8. journal_entries: id, companycode, fiscalyear, accountingdocument,
              accountingdocumentitem, customer, glaccount, referencedocument,
              amountintransactioncurrency, transactioncurrency, postingdate,
              clearingdate, accountingdocumenttype

           9. business_partners: id, businesspartner, customer, businesspartnerfullname,
              businesspartnername, firstname, lastname, industry, businesspartnercategory,
              creationdate, businesspartnerisblocked

           10. products: id, product, producttype, creationdate, productgroup,
               baseunit, division, industrysector, grossweight, netweight,
               weightunit, crossplantstatus

            KEY RELATIONSHIPS:
         - sales_order_headers.soldtoparty = business_partners.customer
         - sales_order_items.salesorder = sales_order_headers.salesorder
         - sales_order_items.material = products.product
         - outbound_delivery_items.referencesddocument = sales_order_headers.salesorder
         - billing_document_items.referencesddocument = sales_order_headers.salesorder
         - payments.invoicereference = billing_document_headers.billingdocument
         - payments.salesdocument = sales_order_headers.salesorder
         - journal_entries.referencedocument = billing_document_headers.billingdocument

           RULES:
               - Return ONLY the SQL query, nothing else
               - No explanations, no markdown, no backticks
               - YOU MUST WRITE ALL COLUMNS IN lowercase (e.g. p.product, not p.PRODUCT)
               - NEVER use uppercase for column names.
               - H2 SQL syntax (standard SQL)
               - Always add LIMIT 100           """;

   public String generateSqlFromQuestion(String question) {
      String prompt = SCHEMA
            + """

                          TASK: Convert the following natural language question into a valid H2 SQL query.

                          CRITICAL RULES:
                          1. Return ONLY the SQL query, nothing else. No markdown formatting or backticks.
                          2. NEVER use table aliases. Do NOT use the AS keyword for tables.
                          3. Always write out the FULL table name for every column (e.g., write `products.product`, NEVER `p.product`).
                          4. ALL tables and columns MUST be in lowercase snake_case exactly as defined in the schema.
                          5. Always add LIMIT 100.

                        EXAMPLE 1:
                  Question: Which products appear in the most billing documents?
                  SQL: SELECT products.product, COUNT(DISTINCT billing_document_items.billingdocument) AS billing_document_count FROM billing_document_items JOIN products ON billing_document_items.material = products.product GROUP BY products.product ORDER BY billing_document_count DESC LIMIT 100

                  EXAMPLE 2:
                  Question: Count the number of sales orders.
                  SQL: SELECT COUNT(sales_order_headers.salesorder) AS total_orders FROM sales_order_headers LIMIT 100
                          ACTUAL QUESTION: """
            + question + """

                  SQL: """;

      return callGemini(prompt);
   }

   public String formatAnswer(String question, String sqlQuery, String rawResults) {
      String prompt = """
                    The user asked: "%s"

                    SQL that was run: %s

                    Raw results from database: %s

                    TASK: Write a complete, detailed answer listing ALL results.
            - Show every row from the results, do not summarize or truncate
            - Format as a clean readable list
            - Do NOT say "and X more" - show everything
            - Do NOT mention SQL or technical details
            - Answer in plain English only
                    """.formatted(question, sqlQuery, rawResults);

      return callGemini(prompt);
   }

   public boolean isRelevantQuestion(String question) {
      if (question == null || question.isBlank()) {
         return false;
      }

      String q = normalize(question);

      String[] businessKeywords = {
            "sales", "sale", "order", "orders", "delivery", "deliveries",
            "delivered", "billing", "billed", "bill", "invoice", "invoices",
            "payment", "payments", "customer", "customers", "product", "products",
            "material", "materials", "shipment", "shipments", "dispatch",
            "journal", "journals", "accounting", "amount", "amounts", "quantity",
            "quantities", "partner", "partners", "document", "documents", "item",
            "items", "flow", "flows", "trace", "broken", "incomplete", "mismatch",
            "reconcile", "status", "date", "count", "total"
      };

      for (String keyword : businessKeywords) {
         if (q.contains(keyword)) {
            return true;
         }
      }

      String[] unrelatedKeywords = {
            "weather", "movie", "movies", "cricket", "football", "sports",
            "recipe", "cooking", "music", "song", "poem", "joke", "news",
            "politics", "history", "capital of", "who is", "stock price"
      };

      for (String keyword : unrelatedKeywords) {
         if (q.contains(keyword)) {
            return false;
         }
      }

      String prompt = """
            You are a guardrail for a SAP Order-to-Cash business data system.
            The system has data about: sales orders, deliveries, billing documents,
            payments, customers, products, and journal entries.

            Question: "%s"

            Reply YES if this question is about business data, orders, deliveries,
            invoices, payments, customers, products, or general data queries.
            Reply NO only for completely unrelated topics like weather, cooking,
            sports, movies, or general knowledge.
            Reply with only YES or NO.
            """.formatted(question);

      String response = callGemini(prompt);
      if (response == null || response.isBlank()) {
         return true;
      }

      String normalizedResponse = normalize(response);
      if (normalizedResponse.matches(".*\\bno\\b.*"))
         return false;
      if (normalizedResponse.matches(".*\\byes\\b.*"))
         return true;

      return true;
   }

   private String normalize(String value) {
      return value.toLowerCase(Locale.ROOT)
            .replace("->", " ")
            .replace('-', ' ')
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
   }

   private String callGemini(String prompt) {
      try {
         String url = apiUrl + "?key=" + apiKey;

         Map<String, Object> body = Map.of(
               "contents", List.of(
                     Map.of("parts", List.of(
                           Map.of("text", prompt)))));

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

         ResponseEntity<String> response = restTemplate.postForEntity(
               url, entity, String.class);

         JsonNode root = objectMapper.readTree(response.getBody());
         return root.path("candidates")
               .path(0)
               .path("content")
               .path("parts")
               .path(0)
               .path("text")
               .asText();

      } catch (Exception e) {
         log.error("Gemini API call failed: {}", e.getMessage());
         return null;
      }
   }
}
