package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.service.CaseLawQueryService;
import com.tys.legalbases.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_CASE_LAW_BASE_PATH)
@RequiredArgsConstructor
public class CaseLawQueryController {

    private final CaseLawQueryService caseLawQueryService;

    @GetMapping
    public ResponseEntity<List<CaseLawDTO>> findAll() {
        return ResponseEntity.ok(caseLawQueryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseLawDTO> findById(@PathVariable Long id) {
        return caseLawQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filing-number/{filingNumber}")
    public ResponseEntity<CaseLawDTO> findByFilingNumber(@PathVariable String filingNumber) {
        return caseLawQueryService.findByFilingNumber(filingNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/topic")
    public ResponseEntity<List<CaseLawDTO>> findByTopic(@RequestParam String topic) {
        return ResponseEntity.ok(caseLawQueryService.findByTopic(topic));
    }

    @GetMapping("/court/{court}")
    public ResponseEntity<List<CaseLawDTO>> findByCourt(@PathVariable String court) {
        return ResponseEntity.ok(caseLawQueryService.findByCourt(court));
    }

    @GetMapping("/court/{court}/topic")
    public ResponseEntity<List<CaseLawDTO>> findByCourtAndTopic(
            @PathVariable String court,
            @RequestParam String topic
    ) {
        return ResponseEntity.ok(caseLawQueryService.findByCourtAndTopic(court, topic));
    }

    @GetMapping("/date")
    public ResponseEntity<List<CaseLawDTO>> findByDateBetween(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(caseLawQueryService.findByDateBetween(from, to));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CaseLawDTO>> fullTextSearch(@RequestParam String q) {
        return ResponseEntity.ok(caseLawQueryService.fullTextSearch(q));
    }
}
