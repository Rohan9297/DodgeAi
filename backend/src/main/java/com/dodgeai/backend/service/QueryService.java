package com.dodgeai.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final EntityManager entityManager;
    private final LLMService llmService;

    public Map<String, Object> handleQuery(String question) {

        // Step 1 — guardrail check
        if (!llmService.isRelevantQuestion(question)) {
            return Map.of(
                    "answer", "This system is designed to answer questions " +
                            "related to the provided dataset only.",
                    "sql", "",
                    "relevant", false);
        }

        // Step 2 — generate SQL from question
        String sql = llmService.generateSqlFromQuestion(question);
        if (sql == null || sql.isBlank()) {
            return Map.of(
                    "answer", "I understood your question, but I could not generate a valid SQL query. " +
                            "Please rephrase and include key business entities like sales order, delivery, billing, or payment.",
                    "sql", "",
                    "relevant", true);
        }

        // Clean up SQL (Gemini sometimes wraps in backticks)
        sql = sql.replaceAll("```sql", "")
                .replaceAll("```", "")
                .trim();

        log.info("Generated SQL: {}", sql);

        // Step 3 — run SQL on H2
        String rawResults;
        try {
            rawResults = executeNativeQuery(sql);
        } catch (Exception e) {
            log.error("SQL execution failed: {}", e.getMessage());
            return Map.of(
                    "answer", "I had trouble querying the data. Please rephrase.",
                    "sql", sql,
                    "relevant", true);
        }

        // Step 4 — format answer via LLM
        String answer = llmService.formatAnswer(question, sql, rawResults);
        if (answer == null) {
            answer = "Raw results: " + rawResults;
        }

        return Map.of(
                "answer", answer,
                "sql", sql,
                "rawResults", rawResults,
                "relevant", true);
    }

    private String executeNativeQuery(String sql) {
        try {
            Query query = entityManager.createNativeQuery(sql);
            List<?> results = query.getResultList();

            if (results.isEmpty())
                return "No results found.";

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Object row : results) {
                if (count++ > 200) {
                    sb.append("... and ").append(results.size() - 50)
                            .append(" more rows");
                    break;
                }
                if (row instanceof Object[] arr) {
                    sb.append(Arrays.toString(arr)).append("\n");
                } else {
                    sb.append(row).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Query failed: " + e.getMessage());
        }
    }
}
