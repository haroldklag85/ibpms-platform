package com.ibpms.poc.application.usecase.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import com.ibpms.poc.application.ports.out.MenuTopologyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MenuLayoutUseCaseTest {

    private MenuTopologyPortFake menuTopologyPortFake;
    private MenuLayoutUseCase menuLayoutUseCase;

    // Zero-Mock: Fake in-memory port implementation sin frameworks de mock
    private static class MenuTopologyPortFake implements MenuTopologyPort {
        private final List<MenuItemDTO> layoutToReturn = new ArrayList<>();
        private Set<String> capturedRoles;

        @Override
        public List<MenuItemDTO> findMenuTreeByRoles(Set<String> roles) {
            this.capturedRoles = roles;
            return layoutToReturn;
        }

        public void setLayoutToReturn(List<MenuItemDTO> layout) {
            this.layoutToReturn.clear();
            if (layout != null) {
                this.layoutToReturn.addAll(layout);
            }
        }

        public Set<String> getCapturedRoles() {
            return capturedRoles;
        }
    }

    @BeforeEach
    void setUp() {
        menuTopologyPortFake = new MenuTopologyPortFake();
        menuLayoutUseCase = new MenuLayoutUseCase(menuTopologyPortFake);
    }

    @Test
    @DisplayName("Debe delegar la búsqueda del layout al puerto MenuTopologyPort usando Fake")
    void getBuildLayoutForUser_DelegatesToPort() {
        // Arrange
        Set<String> roles = Set.of("ROLE_SUPER_ADMIN");
        List<MenuItemDTO> expectedLayout = List.of(new MenuItemDTO("Inicio", "mdi-home", "/home"));
        menuTopologyPortFake.setLayoutToReturn(expectedLayout);

        // Act
        List<MenuItemDTO> layout = menuLayoutUseCase.getBuildLayoutForUser(roles);

        // Assert
        assertThat(layout).isNotNull();
        assertThat(layout.size()).isEqualTo(1);
        assertThat(layout.get(0).getTitle()).isEqualTo("Inicio");
        assertThat(menuTopologyPortFake.getCapturedRoles()).containsExactly("ROLE_SUPER_ADMIN");
    }
}
