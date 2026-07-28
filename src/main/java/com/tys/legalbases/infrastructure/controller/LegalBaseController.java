package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.LegalBaseSearchCriteria;
import com.tys.legalbases.application.dtos.PagedResponseDTO;
import com.tys.legalbases.application.service.LegalBaseService;
import com.tys.legalbases.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_NATIONAL_NORMS_BASE_PATH)
@RequiredArgsConstructor
@CrossOrigin(origins = AppConstants.CROSS_ORIGIN_ALLOWED_ORIGINS, allowedHeaders = AppConstants.CROSS_ORIGIN_ALLOWED_HEADERS)
public class LegalBaseController {

    private final LegalBaseService legalBaseService;

    @GetMapping(AppConstants.API_NATIONAL_NORMS_BY_CRITERIA)
    public ResponseEntity<PagedResponseDTO<LegalBaseDTO>> getAllLegalBases(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(legalBaseService.findAllLegalBases(
                LegalBaseSearchCriteria.builder()
                        .search(search)
                        .page(page)
                        .size(size)
                        .sortBy(sortBy)
                        .sortDir(sortDir)
                        .build()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegalBaseDTO> getLegalBaseById(@PathVariable Long id) {
        return legalBaseService.findLegalBaseById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> save(
            @RequestPart("data") LegalBaseDTO data,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        legalBaseService.save(data, file);
        return ResponseEntity.ok().build();
    }

    @PostMapping(AppConstants.API_NATIONAL_NORMS_INGEST_PATH)
    public ResponseEntity<Void> ingest(@RequestBody LegalBaseDTO data) {
        legalBaseService.ingestFromDto(data);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<LegalBaseDTO>> getAllLegalBases() {
        return ResponseEntity.ok(legalBaseService.findAll());
    }

    @GetMapping(AppConstants.API_NATIONAL_NORMS_RAG_PATH)
    public ResponseEntity<List<LegalBaseDTO>> getAllLegalBasesForRag() {
        return ResponseEntity.ok(legalBaseService.findAllForRag());
    }
}
