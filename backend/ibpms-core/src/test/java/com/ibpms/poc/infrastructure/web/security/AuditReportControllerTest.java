package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.UserEntity;
import com.ibpms.poc.infrastructure.jpa.repository.security.AuditReportRepository;
import com.ibpms.poc.infrastructure.jpa.repository.security.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import com.ibpms.poc.infrastructure.jpa.repository.security.ServiceAccountRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditReportController.class)
class AuditReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuditReportRepository auditReportRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ServiceAccountRepository serviceAccountRepository;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldDownloadCsvReport() throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/security/reports/iso27001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"ibpms_iso27001_report.csv\""))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToNonAdmin() throws Exception {
        // En SecurityConfig restringimos /api/v1/admin/**
        mockMvc.perform(get("/api/v1/admin/security/reports/iso27001"))
                .andExpect(status().isForbidden());
    }
}
