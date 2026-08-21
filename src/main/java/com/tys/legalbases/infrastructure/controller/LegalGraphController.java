package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.LegalGraphDTOs.*;
import com.tys.legalbases.application.service.LegalGraphService;
import com.tys.legalbases.util.AppConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Sources-of-law graph. Consumers: the curator agent (writes) and agent
 * retrieval (reads status / enriched norm before citing).
 */
@RestController
@RequestMapping(AppConstants.API_LEGAL_GRAPH_BASE_PATH)
@RequiredArgsConstructor
public class LegalGraphController {

    private final LegalGraphService graphService;

    @PostMapping("/nodes")
    public ResponseEntity<NodeResponse> createNode(@RequestBody NodeRequest request) {
        return ResponseEntity.ok(graphService.createNode(request));
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<NodeResponse>> searchNodes(@RequestParam("reference") String reference) {
        return ResponseEntity.ok(graphService.searchNodes(reference));
    }

    /** Consumed by legal's vigencia check on the RAG-retrieved norm Documents (only national_norm_id is reliably available there). */
    @GetMapping("/nodes/by-national-norm/{nationalNormId}")
    public ResponseEntity<List<NodeResponse>> findByNationalNormId(@PathVariable("nationalNormId") Long nationalNormId) {
        return ResponseEntity.ok(graphService.findByNationalNormId(nationalNormId));
    }

    @GetMapping("/nodes/{id}/enriched")
    public ResponseEntity<EnrichedNorm> enrichedNorm(@PathVariable("id") Long id) {
        return ResponseEntity.ok(graphService.enrichedNorm(id));
    }

    @PostMapping("/edges")
    public ResponseEntity<EnrichedNorm> createEdge(@RequestBody EdgeRequest request) {
        return ResponseEntity.ok(graphService.createEdge(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
