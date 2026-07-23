package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.dto.BpmnProcessDesignDTO;
import com.ibpms.poc.application.dto.FormDesignDTO;
import com.ibpms.poc.application.service.BpmnDesignService;
import com.ibpms.poc.application.service.FormDesignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormDesignControllerTest {

    @Mock
    private FormDesignService formDesignService;

    @Mock
    private BpmnDesignService bpmnDesignService;

    @InjectMocks
    private FormCatalogController formCatalogController;

    private FormDesignDTO draftSimpleForm;
    private FormDesignDTO activeMasterForm;
    private FormDesignDTO deletedForm;

    @BeforeEach
    void setUp() {
        draftSimpleForm = new FormDesignDTO();
        draftSimpleForm.setTechnicalName("solicitud_draft");
        draftSimpleForm.setName("Solicitud Draft");
        draftSimpleForm.setPattern("SIMPLE");
        draftSimpleForm.setStatus("DRAFT");

        activeMasterForm = new FormDesignDTO();
        activeMasterForm.setTechnicalName("iform_master");
        activeMasterForm.setName("Formulario Maestro");
        activeMasterForm.setPattern("IFORM_MAESTRO");
        activeMasterForm.setStatus("ACTIVE");
        
        deletedForm = new FormDesignDTO();
        deletedForm.setTechnicalName("old_form");
        deletedForm.setName("Deleted Form");
        deletedForm.setPattern("SIMPLE");
        deletedForm.setStatus("DELETED");
    }

    // @Traceability: US-005, CA-39
    @Test
    void getActiveForms_sinProcessKey_retornaTodosLosFormsActivosYDrafts() {
        when(formDesignService.listarCatalogo()).thenReturn(Arrays.asList(draftSimpleForm, activeMasterForm, deletedForm));

        ResponseEntity<List<Map<String, Object>>> response = formCatalogController.getActiveForms(null);

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Ignora DELETED
        
        Map<String, Object> form1 = response.getBody().get(0);
        assertEquals("solicitud_draft", form1.get("id"));
        assertEquals("SIMPLE", form1.get("type"));

        Map<String, Object> form2 = response.getBody().get(1);
        assertEquals("iform_master", form2.get("id"));
        assertEquals("MASTER", form2.get("type"));
        assertEquals(1, form2.get("stages"));
    }

    // @Traceability: US-005, CA-40
    @Test
    void getActiveForms_conPatternSimple_retornaSoloSimples() {
        BpmnProcessDesignDTO process = new BpmnProcessDesignDTO();
        process.setFormPattern("SIMPLE");
        
        when(bpmnDesignService.obtenerPorTechnicalId("proceso_simple")).thenReturn(process);
        when(formDesignService.listarCatalogo()).thenReturn(Arrays.asList(draftSimpleForm, activeMasterForm));

        ResponseEntity<List<Map<String, Object>>> response = formCatalogController.getActiveForms("proceso_simple");

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("solicitud_draft", response.getBody().get(0).get("id"));
    }

    // @Traceability: US-005, CA-39, CA-40
    @Test
    void getActiveForms_conProcessKeyInexistente_ignoraFiltroYRetornaTodos() {
        when(bpmnDesignService.obtenerPorTechnicalId("proceso_invalido")).thenThrow(new RuntimeException("Not found"));
        when(formDesignService.listarCatalogo()).thenReturn(Arrays.asList(draftSimpleForm, activeMasterForm));

        ResponseEntity<List<Map<String, Object>>> response = formCatalogController.getActiveForms("proceso_invalido");

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }
}
