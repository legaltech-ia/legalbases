package com.tys.legalbases.application.service;

import com.tys.legalbases.application.mappers.CaseLawMapper;
import com.tys.legalbases.domain.model.repository.CaseLawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseLawQueryServiceTest {

    @Mock
    private CaseLawRepository caseLawRepository;

    @Mock
    private CaseLawMapper caseLawMapper;

    @InjectMocks
    private CaseLawQueryService caseLawQueryService;

    @Test
    void findByFilingNumberReturnsEmptyWhenRepositoryFindsNothing() {
        when(caseLawRepository.findByFilingNumber("T-000/00")).thenReturn(Optional.empty());

        assertThat(caseLawQueryService.findByFilingNumber("T-000/00")).isEmpty();
    }
}
