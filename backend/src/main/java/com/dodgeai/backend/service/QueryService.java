package com.dodgeai.backend.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private static final String DATASET_ONLY_MESSAGE =
            "This system is designed to answer questions related to the provided dataset only.";

    private final EntityManager entityManager;
    private final LLMService llmService;

    public Map<String, Object> handleQuery(String question) {

        if (!llmService.isRelevantQuestion(question)) {
            return Map.of(
                    "answer", DATASET_ONLY_MESSAGE,
                    "sql", "",
                    "relevant", false);
        }

        String sql = llmService.generateSqlFromQuestion(question);
        if (sql == null || sql.isBlank()) {
            return Map.of(
                    "answer", DATASET_ONLY_MESSAGE,
                    "sql", "",
                    "relevant", false);
        }

        sql = sql.replaceAll("```sql", "")
                .replaceAll("```", "")
                .trim();

        if (!llmService.isSafeDatasetSql(sql)) {
            log.warn("Rejected non-dataset SQL: {}", sql);
            return Map.of(
                    "answer", DATASET_ONLY_MESSAGE,
                    "sql", "",
                    "relevant", false);
        }

        log.info("Generated SQL: {}", sql);

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

            if (results.isEmpty()) {
                return "No results found.";
            }

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
