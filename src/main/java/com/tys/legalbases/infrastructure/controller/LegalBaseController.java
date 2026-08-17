package com.tys.legalbases.infrastructure.controller;

import com.tys.legalbases.application.dtos.IngestResult;
import com.tys.legalbases.application.dtos.LegalBaseDTO;
import com.tys.legalbases.application.dtos.LegalBaseSearchCriteria;
import com.tys.legalbases.application.dtos.PagedResponseDTO;
import com.tys.legalbases.application.service.LegalBaseService;
import com.tys.legalbases.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_NATIONAL_NORMS_BASE_PATH)
@RequiredArgsConstructor
public class LegalBaseController {

    private final LegalBaseService legalBaseService;
    private final ObjectMapper objectMapper;

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

    /**
     * 'data' llega como String y no como DTO tipado a proposito: el encoder
     * multipart de Feign (SpringFormEncoder) solo transporta MultipartFile y
     * campos simples, asi que una parte POJO nunca se envia y el binding falla
     * con "Required part 'data' is not present" (medido el 17 ago 2026). Con un
     * String si viaja, y el BFF puede dejar de usar WebClient para esta llamada.
     */
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResult> save(
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws JsonProcessingException {
        return toResponse(legalBaseService.save(objectMapper.readValue(data, LegalBaseDTO.class), file));
    }

    @PostMapping(AppConstants.API_NATIONAL_NORMS_INGEST_PATH)
    public ResponseEntity<IngestResult> ingest(@RequestBody LegalBaseDTO data) {
        return toResponse(legalBaseService.ingestFromDto(data));
    }

    // ALREADY_EXISTS stays 2xx (200): push callers treat non-2xx as failure and the operation is idempotent.
    private static ResponseEntity<IngestResult> toResponse(IngestResult result) {
        return switch (result.outcome()) {
            case CREATED -> ResponseEntity.status(HttpStatus.CREATED).body(result);
            case ALREADY_EXISTS -> ResponseEntity.ok(result);
        };
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
