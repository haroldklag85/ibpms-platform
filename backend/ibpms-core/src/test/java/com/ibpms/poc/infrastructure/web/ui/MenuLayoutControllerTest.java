package com.ibpms.poc.infrastructure.web.ui;

import com.ibpms.poc.application.service.ui.MenuLayoutService;
import com.ibpms.poc.infrastructure.security.SecurityConfig;
import com.ibpms.poc.infrastructure.web.BaseWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MenuLayoutController.class)
@Import(SecurityConfig.class)
class MenuLayoutControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuLayoutService menuLayoutService;

    @Test
    @DisplayName("GET /api/v1/users/me/menu-layout → returns 200 OK with correct topology when user has all modules")
    @WithMockUser(username = "admin.user")
    void getMenuLayout_AllModulesActive_ReturnsAllGroups() throws Exception {
        when(menuLayoutService.computeTopologyForUser("admin.user"))
                .thenReturn(Set.of("WORKDESK", "SERVICE_DELIVERY", "BAM", "MODELER", "INTEGRATION", "PROJECTS", "ADMINISTRATION"));

        mockMvc.perform(get("/api/v1/users/me/menu-layout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].title").value("groupA"))
                .andExpect(jsonPath("$[0].icon").value("mdi-desktop-mac"))
                .andExpect(jsonPath("$[0].items.length()").value(9))
                .andExpect(jsonPath("$[0].items[0].label").value("portal"))
                .andExpect(jsonPath("$[0].items[0].path").value("/"))
                .andExpect(jsonPath("$[0].items[1].label").value("workdesk"))
                .andExpect(jsonPath("$[0].items[1].path").value("/workdesk"))
                .andExpect(jsonPath("$[0].items[2].label").value("kanban"))
                .andExpect(jsonPath("$[0].items[2].path").value("/kanban"))
                .andExpect(jsonPath("$[0].items[3].label").value("customer360"))
                .andExpect(jsonPath("$[0].items[4].label").value("projectManager"))
                .andExpect(jsonPath("$[0].items[5].label").value("agileHub"))
                .andExpect(jsonPath("$[0].items[6].label").value("intakeTriage"))
                .andExpect(jsonPath("$[0].items[7].label").value("intakeManual"))
                .andExpect(jsonPath("$[0].items[8].label").value("bamDashboard"))
                .andExpect(jsonPath("$[1].title").value("groupB"))
                .andExpect(jsonPath("$[1].icon").value("mdi-shield-alert"))
                .andExpect(jsonPath("$[1].items.length()").value(3))
                .andExpect(jsonPath("$[1].items[0].label").value("identityGovernance"))
                .andExpect(jsonPath("$[1].items[1].label").value("pmoSettings"))
                .andExpect(jsonPath("$[1].items[2].label").value("settings"))
                .andExpect(jsonPath("$[2].title").value("groupC"))
                .andExpect(jsonPath("$[2].icon").value("mdi-hammer-wrench"))
                .andExpect(jsonPath("$[2].items.length()").value(8))
                .andExpect(jsonPath("$[2].items[0].label").value("bpmnDesigner"))
                .andExpect(jsonPath("$[2].items[1].label").value("formsList"))
                .andExpect(jsonPath("$[2].items[2].label").value("formDesigner"))
                .andExpect(jsonPath("$[2].items[3].label").value("dmnCopilot"))
                .andExpect(jsonPath("$[2].items[4].label").value("promptLibrary"))
                .andExpect(jsonPath("$[2].items[5].label").value("genericForm"))
                .andExpect(jsonPath("$[2].items[6].label").value("visualMapper"))
                .andExpect(jsonPath("$[2].items[7].label").value("projectBuilder"))
                .andExpect(jsonPath("$[3].title").value("groupD"))
                .andExpect(jsonPath("$[3].icon").value("mdi-api"))
                .andExpect(jsonPath("$[3].items.length()").value(7))
                .andExpect(jsonPath("$[3].items[0].label").value("connectorCatalog"))
                .andExpect(jsonPath("$[3].items[1].label").value("connectorBuilder"))
                .andExpect(jsonPath("$[3].items[2].label").value("dlqDashboard"))
                .andExpect(jsonPath("$[3].items[3].label").value("inboundMailboxes"))
                .andExpect(jsonPath("$[3].items[4].label").value("documentVault"))
                .andExpect(jsonPath("$[3].items[5].label").value("incidentCenter"))
                .andExpect(jsonPath("$[3].items[6].label").value("instancesManager"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me/menu-layout → filters out groups with no active modules")
    @WithMockUser(username = "some.user")
    void getMenuLayout_LimitedPermissions_ReturnsOnlyAllowedGroups() throws Exception {
        when(menuLayoutService.computeTopologyForUser("some.user"))
                .thenReturn(Set.of("WORKDESK"));

        mockMvc.perform(get("/api/v1/users/me/menu-layout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("groupA"))
                .andExpect(jsonPath("$[0].items.length()").value(3))
                .andExpect(jsonPath("$[0].items[0].label").value("portal"))
                .andExpect(jsonPath("$[0].items[1].label").value("workdesk"))
                .andExpect(jsonPath("$[0].items[2].label").value("kanban"));
    }
}
