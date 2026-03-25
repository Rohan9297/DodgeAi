package com.dodgeai.backend.controller;

import com.dodgeai.backend.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    // Returns all nodes + edges for Cytoscape to render
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGraph() {
        return ResponseEntity.ok(graphService.getGraphData());
    }

    // Returns details of a single node
    @GetMapping("/node/{id}")
    public ResponseEntity<?> getNode(@PathVariable String id) {
        GraphService.GraphNode node = graphService.getNodeById(id);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", node.id);
        result.put("label", node.label);
        result.put("type", node.type);
        result.put("properties", node.properties);
        return ResponseEntity.ok(result);
    }

    // Returns all neighbors of a node (for expand on click)
    @GetMapping("/node/{id}/neighbors")
    public ResponseEntity<List<Map<String, Object>>> getNeighbors(
            @PathVariable String id) {
        return ResponseEntity.ok(graphService.getNeighbors(id));
    }
}