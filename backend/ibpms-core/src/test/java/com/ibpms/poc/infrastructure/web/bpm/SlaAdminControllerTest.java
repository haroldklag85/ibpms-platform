// @Traceability: US-007 - ADR-001
package com.ibpms.poc.infrastructure.web.bpm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ibpms.poc.application.service.bpm.SlaService;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.BusinessHoursEntity;
import com.ibpms.poc.infrastructure.jpa.entity.bpm.HolidayEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class SlaAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SlaService slaService;

    @InjectMocks
    private SlaAdminController slaAdminController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(slaAdminController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /apply?applyRetroactively=false - Should return 200 OK")
    void applySlaMatrix_noRetroactive_returnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sla/apply")
                .param("applyRetroactively", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Matriz guardada para futuras instancias"));
        
        verify(slaService, never()).recalculateActiveSlas();
    }

    @Test
    @DisplayName("POST /apply?applyRetroactively=true - Should return 202 Accepted")
    void applySlaMatrix_withRetroactive_returnsAccepted() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sla/apply")
                .param("applyRetroactively", "true"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("Recálculo en progreso (HTTP 202)"));
        
        verify(slaService, times(1)).recalculateActiveSlas();
    }

    @Test
    @DisplayName("GET /business-hours - Should return 200 OK with default values when empty")
    void getBusinessHours_emptyDB_returnsDefaults() throws Exception {
        when(slaService.getBusinessHours()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/sla/business-hours"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /business-hours - Should return 200 OK and persist entity")
    void updateBusinessHours_returnsPersisted() throws Exception {
        BusinessHoursEntity entity = new BusinessHoursEntity();
        entity.setStartTime(LocalTime.of(9, 0));
        entity.setEndTime(LocalTime.of(18, 0));
        entity.setTimezone("America/New_York");
        entity.setWorkOnWeekends(true);

        when(slaService.getBusinessHours()).thenReturn(Collections.emptyList());
        when(slaService.saveBusinessHours(any(BusinessHoursEntity.class))).thenReturn(entity);

        mockMvc.perform(put("/api/v1/admin/sla/business-hours")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"));
    }

    @Test
    @DisplayName("POST /holidays - Should return 200 and save holiday")
    void addHoliday_savesEntity() throws Exception {
        HolidayEntity holiday = new HolidayEntity();
        holiday.setId(UUID.randomUUID());
        holiday.setHolidayDate(LocalDate.of(2026, 12, 25));
        holiday.setDescription("Navidad");

        when(slaService.addHoliday(any(HolidayEntity.class))).thenReturn(holiday);

        mockMvc.perform(post("/api/v1/admin/sla/holidays")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(holiday)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Navidad"))
                .andExpect(jsonPath("$.holidayDate").value("2026-12-25"));
    }

    @Test
    @DisplayName("GET /holidays - lists holidays")
    void getHolidays_returnsList() throws Exception {
        HolidayEntity holiday = new HolidayEntity();
        holiday.setDescription("Navidad");
        holiday.setHolidayDate(LocalDate.of(2026, 12, 25));

        when(slaService.getHolidays()).thenReturn(Collections.singletonList(holiday));

        mockMvc.perform(get("/api/v1/admin/sla/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Navidad"));
    }

    @Test
    @DisplayName("DELETE /holidays/{id} - deletes holiday")
    void deleteHoliday_removesEntity() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(slaService).deleteHoliday(id);

        mockMvc.perform(delete("/api/v1/admin/sla/holidays/" + id))
                .andExpect(status().isNoContent());

        verify(slaService, times(1)).deleteHoliday(id);
    }
}

