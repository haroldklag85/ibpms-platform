package com.ibpms.poc.application.service.security;

import com.ibpms.poc.AbstractIntegrationTest;
import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.IbpmsProfileEntity;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CA-06: Purga de Roles RBAC desde Lanes ("Roles Fantasma").
 * Esta prueba está diseñada para fallar inicialmente (TDD) para evidenciar 
 * que al desplegar una V2 de un proceso sin un Lane específico, el Rol/Perfil
 * generado en la V1 NO se purga/elimina automáticamente de la base de datos.
 */
class ZombieRolePurgeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DesplegarDefinicionUseCase desplegarDefinicionUseCase;

    @Autowired
    private IbpmsProfileRepository ibpmsProfileRepository;

    @Test
    @DisplayName("TDD CA-06: Los roles/perfiles generados desde BPMN Lanes deben purgarse al eliminarse el Lane en una nueva versión")
    void givenProcessV2WithoutLane_whenDeploying_thenZombieRoleShouldBePurged() {
        // Arrange: XML V1 con un Lane "Aprobadores_Credito"
        String processKey = "Process_Credito";
        String laneName = "Aprobadores_Credito";
        String expectedProfileName = "BPMN_" + processKey + "_" + laneName;

        String xmlV1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
                  <bpmn:process id="%s" isExecutable="true" camunda:historyTimeToLive="180">
                    <bpmn:laneSet id="LaneSet_1">
                      <bpmn:lane id="Lane_1" name="%s">
                        <bpmn:flowNodeRef>Task_1</bpmn:flowNodeRef>
                      </bpmn:lane>
                    </bpmn:laneSet>
                    <bpmn:startEvent id="StartEvent_1" />
                    <bpmn:userTask id="Task_1" name="Aprobar Credito" />
                  </bpmn:process>
                </bpmn:definitions>
                """.formatted(processKey, laneName);

        DeploymentRequestDTO deployV1 = new DeploymentRequestDTO();
        deployV1.setResourceName("credito_v1.bpmn");
        deployV1.setXmlString(xmlV1);

        // Act 1: Desplegar V1
        desplegarDefinicionUseCase.desplegarDesdeWeb(deployV1);

        // Assert 1: El rol/perfil debe haberse creado
        Optional<IbpmsProfileEntity> profileV1 = ibpmsProfileRepository.findByProfileName(expectedProfileName);
        assertTrue(profileV1.isPresent(), "El rol/perfil debe haberse generado a partir de la V1");

        // Arrange 2: XML V2 SIN el Lane "Aprobadores_Credito"
        String xmlV2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
                  <bpmn:process id="%s" isExecutable="true" camunda:historyTimeToLive="180">
                    <bpmn:startEvent id="StartEvent_1" />
                    <bpmn:userTask id="Task_1" name="Aprobar Credito Sin Lane" />
                  </bpmn:process>
                </bpmn:definitions>
                """.formatted(processKey);

        DeploymentRequestDTO deployV2 = new DeploymentRequestDTO();
        deployV2.setResourceName("credito_v2.bpmn");
        deployV2.setXmlString(xmlV2);

        // Act 2: Desplegar V2
        desplegarDefinicionUseCase.desplegarDesdeWeb(deployV2);

        // Assert 2: El rol/perfil "fantasma" debe haber sido eliminado o inactivado (purga)
        Optional<IbpmsProfileEntity> profileV2 = ibpmsProfileRepository.findByProfileName(expectedProfileName);
        
        // ESTO FALLARÁ INTENCIONALMENTE PARA EVIDENCIAR LA DEUDA TÉCNICA CA-06
        assertFalse(profileV2.isPresent(), "Deuda CA-06: El rol/perfil fantasma " + expectedProfileName + " debería haber sido purgado al no existir en la V2.");
    }
}
