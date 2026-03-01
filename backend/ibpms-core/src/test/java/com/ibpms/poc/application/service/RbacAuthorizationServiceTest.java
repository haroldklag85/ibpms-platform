package com.ibpms.poc.application.service;

import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IdpGroupMappingEntity;
import com.ibpms.poc.infrastructure.jpa.entity.ProfileBpmnAssignmentEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IdpGroupMappingRepository;
import com.ibpms.poc.infrastructure.jpa.repository.ProfileBpmnAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacAuthorizationServiceTest {

    @Mock
    private IdpGroupMappingRepository groupMappingRepository;

    @Mock
    private ProfileBpmnAssignmentRepository assignmentRepository;

    @Mock
    private IbpmsProfileRepository profileRepository;

    @InjectMocks
    private RbacAuthorizationService service;

    private IbpmsProfileEntity mockProfile;

    @BeforeEach
    void setUp() {
        mockProfile = new IbpmsProfileEntity();
        mockProfile.setId(UUID.randomUUID());
        mockProfile.setProfileName("BPMN_Proceso_Credito_Analista_Riesgos");
    }

    @Test
    @DisplayName("Debe agrupar y resolver los Lanes BPMN exitosamente de múltiples Grupos EntraID (Multi-Role)")
    void testGetPermittedBpmnLanesForGroups_MultiRole() {
        // Arrange
        String groupId1 = "entra-group-admin";
        String groupId2 = "entra-group-risk";

        IdpGroupMappingEntity mapping1 = new IdpGroupMappingEntity();
        mapping1.setProfile(mockProfile); // Simulamos que mapea al mismo profile por simplificar

        IbpmsProfileEntity mockProfile2 = new IbpmsProfileEntity();
        mockProfile2.setId(UUID.randomUUID());
        IdpGroupMappingEntity mapping2 = new IdpGroupMappingEntity();
        mapping2.setProfile(mockProfile2);

        when(groupMappingRepository.findByIdpGroupId(groupId1)).thenReturn(Optional.of(mapping1));
        when(groupMappingRepository.findByIdpGroupId(groupId2)).thenReturn(Optional.of(mapping2));

        ProfileBpmnAssignmentEntity assignment1 = new ProfileBpmnAssignmentEntity();
        assignment1.setBpmnLaneId("Lane_Admin");

        ProfileBpmnAssignmentEntity assignment2 = new ProfileBpmnAssignmentEntity();
        assignment2.setBpmnLaneId("Lane_Riesgos");

        when(assignmentRepository.findByProfile_Id(mockProfile.getId())).thenReturn(List.of(assignment1));
        when(assignmentRepository.findByProfile_Id(mockProfile2.getId())).thenReturn(List.of(assignment2));

        // Act
        List<String> permittedLanes = service.getPermittedBpmnLanesForGroups(Arrays.asList(groupId1, groupId2));

        // Assert
        assertNotNull(permittedLanes);
        assertEquals(2, permittedLanes.size());
        assertTrue(permittedLanes.contains("Lane_Admin"));
        assertTrue(permittedLanes.contains("Lane_Riesgos"));
    }

    @Test
    @DisplayName("Debe vincular correctamente un nuevo Carril a un Perfil autogenerando Entidades de Seguridad")
    void testBindLaneToProfile_NewProfileAndAssignment() {
        // Arrange
        when(profileRepository.findByProfileName("BPMN_Credito_Ejecutivo")).thenReturn(Optional.empty());
        when(profileRepository.save(any(IbpmsProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Note: we can't reliably mock assignments list if profile is new (no ID generated in mock yet unless we hardcode it),
        // but since findByProfileName returned empty, assignmentRepository will be called with a null ID or mock ID.
        // We handle any list returned by assignmentRepository:
        when(assignmentRepository.findByProfile_Id(any())).thenReturn(List.of());
        
        when(assignmentRepository.save(any(ProfileBpmnAssignmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.bindLaneToProfile("Credito", "Lane_Ejecutivo", "BPMN_Credito_Ejecutivo", "Autogenerado");

        // Assert
        ArgumentCaptor<IbpmsProfileEntity> profileCaptor = ArgumentCaptor.forClass(IbpmsProfileEntity.class);
        verify(profileRepository, times(1)).save(profileCaptor.capture());
        assertEquals("BPMN_Credito_Ejecutivo", profileCaptor.getValue().getProfileName());

        ArgumentCaptor<ProfileBpmnAssignmentEntity> assignmentCaptor = ArgumentCaptor.forClass(ProfileBpmnAssignmentEntity.class);
        verify(assignmentRepository, times(1)).save(assignmentCaptor.capture());
        assertEquals("Credito", assignmentCaptor.getValue().getBpmnProcessKey());
        assertEquals("Lane_Ejecutivo", assignmentCaptor.getValue().getBpmnLaneId());
    }
}
