package com.ibpms.poc.application.usecase.dmn;

import com.ibpms.poc.infrastructure.jpa.entity.dmn.DmnModelEntity;
import com.ibpms.poc.infrastructure.jpa.repository.dmn.DmnModelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DmnGovernanceUseCaseTest {

    @Mock
    private DmnModelRepository dmnRepository;

    @InjectMocks
    private DmnGovernanceUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateDmnContent_WhenManualEdit_ShouldSetIsManualAndLog() {
        // Arrange
        String dmnId = "dmn-123";
        String tenantId = "tenant-a";
        String newXml = "<xml>new</xml>";
        boolean isManual = true;

        DmnModelEntity existing = new DmnModelEntity();
        existing.setId(dmnId);
        existing.setTenantId(tenantId);
        existing.setStatus("DRAFT");
        existing.setXmlContent("<xml>old</xml>");

        when(dmnRepository.findById(dmnId)).thenReturn(Optional.of(existing));
        when(dmnRepository.save(any(DmnModelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DmnModelEntity updated = useCase.updateDmnContent(dmnId, newXml, tenantId, isManual);

        // Assert
        ArgumentCaptor<DmnModelEntity> captor = ArgumentCaptor.forClass(DmnModelEntity.class);
        verify(dmnRepository).save(captor.capture());
        
        DmnModelEntity saved = captor.getValue();
        assertEquals(newXml, saved.getXmlContent());
        assertTrue(saved.getIsManual());
    }
}
