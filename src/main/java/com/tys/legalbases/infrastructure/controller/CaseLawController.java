package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.service.CaseLawService;
import com.tys.legalbases.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_CASE_LAW_BASE_PATH)
@RequiredArgsConstructor
@CrossOrigin(origins = AppConstants.CROSS_ORIGIN_ALLOWED_ORIGINS)
public class CaseLawController {

    private final CaseLawService caseLawService;

    @GetMapping
    public ResponseEntity<List<CaseLawDTO>> findAll() {
        return ResponseEntity.ok(caseLawService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseLawDTO> findById(@PathVariable Long id) {
        return caseLawService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filing-number/{filingNumber}")
    public ResponseEntity<CaseLawDTO> findByFilingNumber(@PathVariable String filingNumber) {
        return caseLawService.findByFilingNumber(filingNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/topic")
    public ResponseEntity<List<CaseLawDTO>> findByTopic(@RequestParam String topic) {
        return ResponseEntity.ok(caseLawService.findByTopic(topic));
    }

    @GetMapping("/court/{court}")
    public ResponseEntity<List<CaseLawDTO>> findByCourt(@PathVariable String court) {
        return ResponseEntity.ok(caseLawService.findByCourt(court));
    }

    @GetMapping("/court/{court}/topic")
    public ResponseEntity<List<CaseLawDTO>> findByCourtAndTopic(
            @PathVariable String court,
            @RequestParam String topic
    ) {
        return ResponseEntity.ok(caseLawService.findByCourtAndTopic(court, topic));
    }

    @GetMapping("/date")
    public ResponseEntity<List<CaseLawDTO>> findByDateBetween(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(caseLawService.findByDateBetween(from, to));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CaseLawDTO>> fullTextSearch(@RequestParam String q) {
        return ResponseEntity.ok(caseLawService.fullTextSearch(q));
    }

    @PostMapping
    public ResponseEntity<CaseLawDTO> save(@RequestBody CaseLawDTO dto) {
        return ResponseEntity.ok(caseLawService.save(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> saveAll(@RequestBody List<CaseLawDTO> dtos) {
        caseLawService.saveAll(dtos);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sourceId}/precedent/{targetId}")
    public ResponseEntity<Void> addPrecedentRelation(
            @PathVariable Long sourceId,
            @PathVariable Long targetId,
            @RequestParam String relationType,
            @RequestParam(required = false) String context
    ) {
        caseLawService.addPrecedentRelation(sourceId, targetId, relationType, context);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        caseLawService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
