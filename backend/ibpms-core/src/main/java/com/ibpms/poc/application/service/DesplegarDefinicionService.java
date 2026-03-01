package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.DeploymentRequestDTO;
import com.ibpms.poc.application.port.in.DesplegarDefinicionUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.application.port.out.RbacPort;
import org.springframework.stereotype.Service;

@Service
public class DesplegarDefinicionService implements DesplegarDefinicionUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final RbacPort rbacPort;

    public DesplegarDefinicionService(ProcesoBpmPort procesoBpmPort, RbacPort rbacPort) {
        this.procesoBpmPort = procesoBpmPort;
        this.rbacPort = rbacPort;
    }

    @Override
    public void desplegarDesdeWeb(DeploymentRequestDTO request) {
        if (request == null || request.getXmlString() == null || request.getXmlString().trim().isEmpty()) {
            throw new IllegalArgumentException("El XML del modelo no puede estar vacío.");
        }

        String resourceName = request.getResourceName();
        if (resourceName == null || resourceName.trim().isEmpty()) {
            resourceName = "proceso_dinamico_" + System.currentTimeMillis() + ".bpmn";
        } else if (!resourceName.endsWith(".bpmn") && !resourceName.endsWith(".dmn")) {
            resourceName += ".bpmn"; // Asegurar extensión válida por defecto
        }

        // Si es un BPMN, intentamos extraer los Roles (Lanes) y autogenerarlos (US-029)
        if (resourceName.endsWith(".bpmn")) {
            generarRolesDesdeLanes(request.getXmlString());
        }

        procesoBpmPort.desplegarProceso(resourceName, request.getXmlString());
    }

    private void generarRolesDesdeLanes(String xmlString) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            // Ignorar namespaces para búsqueda simple por nombre de etiqueta
            factory.setNamespaceAware(false);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(xmlString.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

            org.w3c.dom.NodeList processNodes = doc.getElementsByTagName("bpmn:process");
            if (processNodes.getLength() == 0) {
                processNodes = doc.getElementsByTagName("process");
            }

            for (int i = 0; i < processNodes.getLength(); i++) {
                org.w3c.dom.Element processElement = (org.w3c.dom.Element) processNodes.item(i);
                String processId = processElement.getAttribute("id");

                org.w3c.dom.NodeList laneNodes = processElement.getElementsByTagName("bpmn:lane");
                if (laneNodes.getLength() == 0) {
                    laneNodes = processElement.getElementsByTagName("lane");
                }

                for (int j = 0; j < laneNodes.getLength(); j++) {
                    org.w3c.dom.Element laneElement = (org.w3c.dom.Element) laneNodes.item(j);
                    String laneId = laneElement.getAttribute("id");
                    String laneName = laneElement.getAttribute("name");

                    if (laneId != null && !laneId.isEmpty()) {
                        String friendlyName = (laneName != null && !laneName.isEmpty()) ? laneName : laneId;
                        String roleName = "BPMN_" + processId + "_" + friendlyName.replaceAll("\\s+", "_");
                        String description = "Autogenerado desde el Carril '" + friendlyName + "' del proceso '"
                                + processId + "'";

                        rbacPort.bindLaneToProfile(processId, laneId, roleName, description);
                    }
                }
            }
        } catch (Exception e) {
            // Loguear error pero no interrumpir el despliegue a Camunda (Fail-Safe)
            System.err.println("Advertencia: No se pudieron extraer los Roles RBAC del BPMN. " + e.getMessage());
        }
    }
}
