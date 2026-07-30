package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.CaseLawDTO;
import com.tys.legalbases.application.service.CaseLawCommandService;
import com.tys.legalbases.application.service.PrecedentRelationService;
import com.tys.legalbases.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_CASE_LAW_BASE_PATH)
@RequiredArgsConstructor
public class CaseLawCommandController {

    private final CaseLawCommandService caseLawCommandService;
    private final PrecedentRelationService precedentRelationService;

    @PostMapping
    public ResponseEntity<CaseLawDTO> save(@RequestBody CaseLawDTO dto) {
        return ResponseEntity.ok(caseLawCommandService.save(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> saveAll(@RequestBody List<CaseLawDTO> dtos) {
        caseLawCommandService.saveAll(dtos);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sourceId}/precedent/{targetId}")
    public ResponseEntity<Void> addPrecedentRelation(
            @PathVariable Long sourceId,
            @PathVariable Long targetId,
            @RequestParam String relationType,
            @RequestParam(required = false) String context
    ) {
        precedentRelationService.addPrecedentRelation(sourceId, targetId, relationType, context);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        caseLawCommandService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
