package com.ibpms.poc.infrastructure.camunda.listener;

import com.ibpms.poc.domain.model.security.SecurityRole;
import com.ibpms.poc.domain.port.out.security.SecurityRolePort;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProcessRoleDiscoveryListener extends AbstractBpmnParseListener {

    private static final Logger log = LoggerFactory.getLogger(ProcessRoleDiscoveryListener.class);
    private final SecurityRolePort rolePort;

    public ProcessRoleDiscoveryListener(SecurityRolePort rolePort) {
        this.rolePort = rolePort;
    }

    /**
     * Intercepta nativamente la etiqueta <bpmn:lane> del XML.
     * Se ejecuta milisegundos antes de que el archivo se guarde en la BD Camunda.
     */
    @Override
    public void parseLane(Element laneElement, org.camunda.bpm.engine.impl.pvm.process.Lane lane,
            org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl processDefinition) {
        String laneId = lane.getId();
        String laneName = lane.getName() != null ? lane.getName() : laneId;
        String processId = processDefinition.getKey();

        String generatedRoleName = "PROCESS:" + processId + ":" + laneName.replace(" ", "_");

        log.info("[BPMN Discovery] Auto-Generando Rol a partir de BPMN Lane. Process: {}, Lane: {}", processId,
                laneName);

        SecurityRole newRole = new SecurityRole(
                UUID.randomUUID(),
                generatedRoleName,
                "Auto-descubierto desde despliegue del proceso " + processId,
                "PROCESS_GENERATED",
                processId,
                laneId);

        // Patrón Idempotente (Upsert) delegado al DB Port.
        try {
            rolePort.saveOrUpdateRole(newRole);
        } catch (Exception e) {
            log.warn("Fallo al guardar el rol auto-descubierto: {}", generatedRoleName, e);
        }
    }
}
