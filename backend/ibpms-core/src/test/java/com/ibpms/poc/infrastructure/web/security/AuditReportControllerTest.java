// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.security.AuditReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import com.ibpms.poc.infrastructure.web.BaseWebMvcTest;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditReportController.class)
@org.springframework.context.annotation.Import(com.ibpms.poc.infrastructure.security.SecurityConfig.class)
class AuditReportControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditReportService auditReportService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN", username = "testuser")
    void shouldDownloadCsvReport() throws Exception {
        AuditReportService.ReportResult mockResult = new AuditReportService.ReportResult(
            "dummy csv".getBytes(), "hash123"
        );
        when(auditReportService.generateIso27001Report("testuser")).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/security/audit/reports/iso27001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"ibpms_iso27001_report.csv\""))
                .andExpect(header().string("X-Report-Hash", "sha256:hash123"))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        // En SecurityConfig restringimos /api/v1/security/audit/** a admin
        mockMvc.perform(post("/api/v1/security/audit/reports/iso27001"))
                .andExpect(status().isForbidden());
    }
}
