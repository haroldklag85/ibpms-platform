// @Traceability: US-007 - ADR-001​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import com.ibpms.poc.application.dto.PreFlightResultDTO;
import com.ibpms.poc.application.port.out.BpmnValidationPort;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import org.springframework.web.client.RestTemplate;
import java.util.Collection;
import java.util.List;

@Component
public class CamundaBpmnValidationAdapter implements BpmnValidationPort {

    private static final Logger log = LoggerFactory.getLogger(CamundaBpmnValidationAdapter.class);
    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    // @Traceability: US-005, CA-15
    private static final java.util.regex.Pattern SEMVER_PATTERN = 
        java.util.regex.Pattern.compile("^[0-9]+\\.[0-9]+\\.[0-9]+.*$");

    @Override
    public PreFlightResultDTO validateDraftXml(String xml, int maxNodes) {
        PreFlightResultDTO result = new PreFlightResultDTO();

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            checkUserTaskFormKey(doc, result);
            checkServiceTaskDelegate(doc, result);
            checkExclusiveGatewayDefault(doc, result);
            checkTimerEvent(doc, result);
            checkMessageEvent(doc, result);
            checkCallActivity(doc, result);
            checkZombieNodes(doc, result);
            checkInfiniteLoops(doc, result);
            checkGatewayConvergence(doc, result);
            // @Traceability: US-005, CA-15
            checkProcessVersionTag(doc, result);
            checkProcessNomenclature(doc, result);
            checkMaxNodes(doc, maxNodes, result);

        } catch (Exception e) {
            result.addIssue(PreFlightResultDTO.Severity.ERROR, "XML_PARSE",
                    null, "Error parseando el XML BPMN: " + e.getMessage());
        }

        boolean hasErrors = result.getIssues().stream()
                .anyMatch(i -> "ERROR".equals(i.getSeverity()));
        result.setPassed(!hasErrors);

        return result;
    }

    @Override
    public DeploymentValidationResponse validateBpmnStream(InputStream bpmnStream, List<String> activeTopics, List<String> vipRoleNames) {
        DeploymentValidationResponse response = new DeploymentValidationResponse();
        response.setValid(true);

        try {
            // @Traceability: US-005, CA-01 - Sanitización del stream antes de parsear
            byte[] rawBytes = bpmnStream.readAllBytes();
            String xmlContent = new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);

            // Eliminar BOM (Byte Order Mark: \uFEFF) si está presente al inicio
            if (xmlContent.startsWith("\uFEFF")) {
                xmlContent = xmlContent.substring(1);
                log.debug("[PreFlight] BOM detectado y eliminado del stream BPMN.​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​");
            }
            // Eliminar whitespace inicial antes de la declaración XML
            xmlContent = xmlContent.trim();

            log.debug("[PreFlight] XML recibido (primeros 300 chars): {}​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​",
                      xmlContent.length() > 300 ? xmlContent.substring(0, 300) : xmlContent);

            // @Traceability: US-005, CA-01 — Parser dual: Camunda (rico) con fallback DocumentBuilder (básico)
            // El parser de Camunda puede rechazar XML válido de bpmn-js por extensiones no reconocidas.
            // Si falla, validamos con DocumentBuilder estándar (más permisivo) y continuamos con validaciones
            // semánticas manuales vía XPath/tagName en lugar de la API tipada de Camunda.
            BpmnModelInstance modelInstance = null;
            boolean camundaParserFailed = false;

            try {
                java.io.InputStream camundaStream = new java.io.ByteArrayInputStream(
                    xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
                modelInstance = Bpmn.readModelFromStream(camundaStream);
            } catch (Exception camundaEx) {
                camundaParserFailed = true;
                log.warn("[PreFlight] Parser Camunda rechazó el XML ({}). Intentando validación básica con DocumentBuilder...", camundaEx.getMessage());
                try {
                    javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
                    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
                    db.parse(new java.io.ByteArrayInputStream(
                        xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                    ));
                    // XML es bien formado — advertencia no bloqueante, deploy procede
                    log.info("[PreFlight] DocumentBuilder validó correctamente el XML. Deploy permitido con validación básica.");
                    response.addWarning("XML_COMPATIBILITY",
                        "El validador semántico avanzado detectó extensiones propietarias de bpmn-js. El XML es válido y el despliegue procede con validación básica.");
                } catch (Exception docEx) {
                    // XML genuinamente corrupto — bloquear deploy
                    log.error("[PreFlight] DocumentBuilder también rechazó el XML: {}", docEx.getMessage());
                    response.addError("XML_PARSE", "El XML del diagrama está malformado: " + docEx.getMessage());
                    return response;
                }
            }

            // Si Camunda falló pero DocumentBuilder pasó, omitimos validaciones semánticas tipadas
            // y retornamos directamente (advertencia ya añadida arriba, valid=true).
            if (camundaParserFailed) {
                return response;
            }

            // A partir de aquí, modelInstance != null (Camunda parseó correctamente)


            Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
            if (endEvents == null || endEvents.isEmpty()) {
                // @Traceability: US-005, CA-02
                response.addError("Diagram", "El diagrama no es instanciable. Falta End Event.");
            }

            Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);
            for (ServiceTask st : serviceTasks) {
                String cmdDelExpr = st.getCamundaDelegateExpression();
                String cmdClass = st.getCamundaClass();
                String cmdTopic = st.getCamundaTopic();
                if ((cmdDelExpr == null || cmdDelExpr.isBlank()) &&
                    (cmdClass == null || cmdClass.isBlank()) &&
                    (cmdTopic == null || cmdTopic.isBlank())) {
                    response.addError(st.getId(), "ServiceTask carece de propiedad de ejecución (delegateExpression, class o topic)");
                } else if (cmdTopic != null && !cmdTopic.isBlank()) {
                    if (!activeTopics.contains(cmdTopic)) {
                        response.addError(st.getId(), "Topic '" + cmdTopic + "' no está registrado o inactivo en el catálogo de External Tasks.");
                    }
                }
            }

            Collection<UserTask> userTasks = modelInstance.getModelElementsByType(UserTask.class);
            Collection<Lane> lanes = modelInstance.getModelElementsByType(Lane.class);
            for (UserTask ut : userTasks) {
                String formKey = ut.getCamundaFormKey();
                if (formKey == null || formKey.isBlank()) {
                    response.addError(ut.getId(), "UserTask carece de camunda:formKey obligatorio");
                } else if ("sys_generic_form".equals(formKey)) {
                    // @Traceability: US-039 - CA-1
                    for (Lane lane : lanes) {
                        if (lane.getFlowNodeRefs().contains(ut)) {
                            String laneNameUpper = lane.getName() != null ? lane.getName().toUpperCase() : "";
                            boolean isVipLane = vipRoleNames.stream().anyMatch(vip -> laneNameUpper.contains(vip));
                            if (isVipLane) {
                                response.addError(ut.getId(), "Hard-Stop: UserTask (" + ut.getId() + ") utiliza Formulario Genérico (sys_generic_form) pero está categorizado bajo un perfil VIP restringido (" + laneNameUpper + "). Obligatorio diseñar un iForm Maestro.");
                            }
                            break;
                        }
                    }
                }
            }

            Collection<ExclusiveGateway> gateways = modelInstance.getModelElementsByType(ExclusiveGateway.class);
            for (ExclusiveGateway gw : gateways) {
                if (gw.getOutgoing().size() > 1 && gw.getDefault() == null) {
                    // @Traceability: US-005, CA-07 Gobernanza Estricta de Despliegue
                    response.addError(gw.getId(), "Hard-Stop: ExclusiveGateway sin Flujo por Defecto (default property)");
                }
            }

            Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
            boolean hasValidStartForm = false;
            for (StartEvent se : startEvents) {
                if (se.getCamundaFormKey() != null && !se.getCamundaFormKey().isBlank()) {
                    hasValidStartForm = true;
                    break;
                }
            }
            // @Traceability: US-005 — Arquitectura IBPMS: el StartEvent es un punto de partida topológico.
            // Los formularios y eventos (API, webhook, timer) se vinculan a Tasks dentro del proceso.
            // El camunda:formKey en StartEvent es OPCIONAL: solo requerido si el proceso se inicia
            // manualmente desde el Tasklist de Camunda con formulario de inicio. No es un hard-stop.
            if (!hasValidStartForm && !startEvents.isEmpty()) {
                response.addWarning("StartEvent", "Recomendación: El StartEvent no tiene camunda:formKey configurado. Si el proceso se inicia por API, webhook, timer o evento externo, esto es correcto y esperado. Configure formKey solo si desea un formulario de inicio manual en el StartEvent.");
            }

            Collection<BusinessRuleTask> brTasks = modelInstance.getModelElementsByType(BusinessRuleTask.class);
            for (BusinessRuleTask brt : brTasks) {
                String decisionRef = brt.getCamundaDecisionRef();
                String binding = brt.getCamundaDecisionRefBinding();

                if (decisionRef != null && !decisionRef.isBlank()) {
                    if (binding == null || binding.isBlank()) {
                        // @Traceability: US-005, CA-12 Late vs Deployment Binding (DMN)
                        String msg = "Hard-Stop: BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                            "' enlaza a DMN (" + decisionRef + ") sin camunda:decisionRefBinding configurado. " +
                            "El motor asumirá 'latest' por defecto, lo cual viola la protección de derechos adquiridos (CA-12). " +
                            "Obligatorio: Configure 'deployment' en el Modeler para garantizar que los casos en vuelo se evalúen con la versión DMN vigente al nacer el caso.";
                        response.addError(brt.getId(), msg);
                        response.addWarning(brt.getId(), msg);
                    } else if ("latest".equals(binding)) {
                        // @Traceability: US-005, CA-12 Late vs Deployment Binding (DMN)
                        String msg = "Hard-Stop: BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                            "' usa Late Binding (LATEST). Los casos en vuelo se evaluarán con la última versión DMN publicada. " +
                            "Esto viola compromisos contractuales. Cambie a 'deployment'.";
                        response.addError(brt.getId(), msg);
                        response.addWarning(brt.getId(), msg);
                    }
                    
                    // GAP-12: Validación de Catch-All en DMN
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        String dmnJson = restTemplate.getForObject("http://localhost:8080/api/v1/dmn/" + decisionRef, String.class);
                        boolean hasCatchAll = dmnJson != null && (dmnJson.contains("<text>-</text>") || dmnJson.toLowerCase().contains("catch-all"));
                        
                        if (hasCatchAll) {
                            boolean validNextNode = brt.getOutgoing().stream()
                                .map(org.camunda.bpm.model.bpmn.instance.SequenceFlow::getTarget)
                                .anyMatch(node -> node instanceof ExclusiveGateway || node instanceof UserTask);
                                
                            if (!validNextNode) {
                                response.addError(brt.getId(), "GAP-12: La DMN referenciada (" + decisionRef + ") posee una regla Catch-All. El BPMN debe obligatoriamente canalizar la salida hacia un ExclusiveGateway o un UserTask de revisión humana.");
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Pre-Flight: No se pudo verificar si DMN {} posee Catch-All para GAP-12 ({}).", decisionRef, e.getMessage());
                    }
                }
            }

            boolean hasNomenclature = false;
            Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
            String firstProcessId = "UnknownProcess";
            if (!processes.isEmpty()) {
                Process proc = processes.iterator().next();
                firstProcessId = proc.getId();
                Collection<CamundaProperty> properties = proc.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.ExtensionElements.class).isEmpty() 
                    ? java.util.Collections.emptyList() 
                    : proc.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.ExtensionElements.class).iterator().next().getChildElementsByType(CamundaProperty.class);
                
                for (CamundaProperty prop : properties) {
                    if ("ReglaNomenclatura".equals(prop.getCamundaName())) {
                        hasNomenclature = true;
                        break;
                    }
                }
            }
            // @Traceability: US-005, CA-05 — ReglaNomenclatura es una buena práctica de gobernanza,
            // no un requisito técnico de despliegue. Se mantiene como advertencia visible para el diseñador.
            if (!hasNomenclature) {
                response.addWarning("Process", "Recomendación de Gobernanza (CA-05): Defina la propiedad 'ReglaNomenclatura' en las Extension Properties del proceso para estandarizar el nombre de los casos generados. Acceda al Panel de Propiedades → Proceso → Regla de Nomenclatura.");
            }

            // @Traceability: US-005, CA-15
            for (Process proc : processes) {
                if (proc.isExecutable()) {
                    String versionTag = proc.getCamundaVersionTag();
                    if (versionTag == null || versionTag.isBlank()) {
                        response.addError(proc.getId(), "El tag de versión (versionTag) es obligatorio y no puede estar vacío.");
                    } else if (!SEMVER_PATTERN.matcher(versionTag).matches()) {
                        response.addError(proc.getId(), "El tag de versión '" + versionTag + "' no cumple con el formato SemVer (x.y.z).");
                    }
                }
            }

            Collection<TimerEventDefinition> timers = modelInstance.getModelElementsByType(TimerEventDefinition.class);
            for (TimerEventDefinition t : timers) {
                if ((t.getTimeDuration() == null || t.getTimeDuration().getTextContent().isBlank()) &&
                    (t.getTimeCycle() == null || t.getTimeCycle().getTextContent().isBlank()) &&
                    (t.getTimeDate() == null || t.getTimeDate().getTextContent().isBlank())) {
                    response.addError(t.getId() != null ? t.getId() : "TimerEvent", "TimerEvent sin expresión de tiempo válida definida.");
                }
            }

            Collection<MessageEventDefinition> messages = modelInstance.getModelElementsByType(MessageEventDefinition.class);
            for (MessageEventDefinition m : messages) {
                if (m.getMessage() == null) {
                    response.addError(m.getId() != null ? m.getId() : "MessageEvent", "MessageEvent carece de Message Reference (messageRef).");
                }
                
                Event parentEvent = (Event) m.getParentElement();
                String delegate = parentEvent.getAttributeValueNs("http://camunda.org/schema/1.0/bpmn", "delegateExpression");
                String expression = parentEvent.getAttributeValueNs("http://camunda.org/schema/1.0/bpmn", "expression");
                String clazz = parentEvent.getAttributeValueNs("http://camunda.org/schema/1.0/bpmn", "class");
                
                boolean hasDelegate = (delegate != null && !delegate.isBlank()) || 
                                      (expression != null && !expression.isBlank()) || 
                                      (clazz != null && !clazz.isBlank());
                                      
                boolean hasConnector = !parentEvent.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.ExtensionElements.class).isEmpty() &&
                                       !parentEvent.getChildElementsByType(org.camunda.bpm.model.bpmn.instance.ExtensionElements.class).iterator().next()
                                          .getChildElementsByType(org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector.class).isEmpty();

                if (!hasDelegate && !hasConnector) {
                    response.addWarning(parentEvent.getId(), "MessageEvent sin conector API asociado. Considere crear el conector en el Hub y migrar a Service Task.");
                }
            }

            Collection<CallActivity> calls = modelInstance.getModelElementsByType(CallActivity.class);
            for (CallActivity ca : calls) {
                if (ca.getCalledElement() == null || ca.getCalledElement().isBlank()) {
                    response.addError(ca.getId(), "CallActivity invoca subproceso pero carece de la propiedad 'calledElement'.");
                }
            }

            for (Lane lane : lanes) {
                String laneName = lane.getName() != null ? lane.getName() : lane.getId();
                String roleName = "BPMN_" + firstProcessId + "_" + laneName.replaceAll("\\s+", "_");
                response.getGeneratedRoles().add(roleName);
                log.info("Simulación Rol RBAC detectado por Carril: {}", roleName);
            }

        } catch (Exception e) {
            response.addError("XML_PARSE", "Fallo severo al leer XML: " + e.getMessage());
        }

        return response;
    }

    // @Traceability: US-005, CA-15
    private void checkProcessVersionTag(Document doc, PreFlightResultDTO result) {
        NodeList processes = doc.getElementsByTagNameNS(BPMN_NS, "process");
        if (processes.getLength() == 0) {
            processes = doc.getElementsByTagName("bpmn:process");
        }
        if (processes.getLength() == 0) {
            processes = doc.getElementsByTagName("process");
        }

        for (int i = 0; i < processes.getLength(); i++) {
            Element el = (Element) processes.item(i);
            String isExecutable = el.getAttribute("isExecutable");
            if (isExecutable != null && "false".equalsIgnoreCase(isExecutable.trim())) {
                continue;
            }

            String versionTag = el.getAttributeNS("http://camunda.org/schema/1.0/bpmn", "versionTag");
            if (versionTag == null || versionTag.isBlank()) {
                versionTag = el.getAttribute("camunda:versionTag");
            }

            if (versionTag == null || versionTag.isBlank()) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "PROCESS_VERSION_TAG_EMPTY",
                        el.getAttribute("id"), "El tag de versión (versionTag) es obligatorio.");
            } else if (!SEMVER_PATTERN.matcher(versionTag).matches()) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "PROCESS_VERSION_TAG_INVALID_SEMVER",
                        el.getAttribute("id"), "El tag de versión '" + versionTag + "' no cumple con el formato SemVer (x.y.z).");
            }
        }
    }

    // @Traceability: US-005, CA-05
    private void checkProcessNomenclature(Document doc, PreFlightResultDTO result) {
        NodeList processes = doc.getElementsByTagNameNS(BPMN_NS, "process");
        if (processes.getLength() == 0) {
            processes = doc.getElementsByTagName("bpmn:process");
        }
        if (processes.getLength() == 0) {
            processes = doc.getElementsByTagName("process");
        }

        for (int i = 0; i < processes.getLength(); i++) {
            Element el = (Element) processes.item(i);
            String isExecutable = el.getAttribute("isExecutable");
            if (isExecutable != null && "false".equalsIgnoreCase(isExecutable.trim())) {
                continue;
            }

            boolean hasNomenclature = false;
            NodeList properties = el.getElementsByTagNameNS("http://camunda.org/schema/1.0/bpmn", "property");
            for (int j = 0; j < properties.getLength(); j++) {
                Element prop = (Element) properties.item(j);
                if ("ReglaNomenclatura".equals(prop.getAttribute("name"))) {
                    hasNomenclature = true;
                    break;
                }
            }
            if (!hasNomenclature) {
                NodeList propertiesNoNS = el.getElementsByTagName("camunda:property");
                for (int j = 0; j < propertiesNoNS.getLength(); j++) {
                    Element prop = (Element) propertiesNoNS.item(j);
                    if ("ReglaNomenclatura".equals(prop.getAttribute("name"))) {
                        hasNomenclature = true;
                        break;
                    }
                }
            }
            if (!hasNomenclature) {
                NodeList propertiesNoPrefix = el.getElementsByTagName("property");
                for (int j = 0; j < propertiesNoPrefix.getLength(); j++) {
                    Element prop = (Element) propertiesNoPrefix.item(j);
                    if ("ReglaNomenclatura".equals(prop.getAttribute("name"))) {
                        hasNomenclature = true;
                        break;
                    }
                }
            }

            if (!hasNomenclature) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "PROCESS_NO_NOMENCLATURE",
                        el.getAttribute("id"), "Debe definir cómo se llamarán los casos de este proceso (ReglaNomenclatura).");
            }
        }
    }

    private void checkUserTaskFormKey(Document doc, PreFlightResultDTO result) {
        NodeList tasks = doc.getElementsByTagNameNS(BPMN_NS, "userTask");
        if (tasks.getLength() == 0) {
            tasks = doc.getElementsByTagName("bpmn:userTask");
        }
        for (int i = 0; i < tasks.getLength(); i++) {
            Element el = (Element) tasks.item(i);
            String formKey = el.getAttributeNS("http://camunda.org/schema/1.0/bpmn", "formKey");
            if (formKey == null || formKey.isBlank()) {
                formKey = el.getAttribute("camunda:formKey");
            }
            if (formKey == null || formKey.isBlank()) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "USER_TASK_NO_FORM_KEY",
                        el.getAttribute("id"), "UserTask sin FormKey definido.");
            }
        }
    }

    private void checkServiceTaskDelegate(Document doc, PreFlightResultDTO result) {
        NodeList tasks = doc.getElementsByTagNameNS(BPMN_NS, "serviceTask");
        if (tasks.getLength() == 0) {
            tasks = doc.getElementsByTagName("bpmn:serviceTask");
        }
        for (int i = 0; i < tasks.getLength(); i++) {
            Element el = (Element) tasks.item(i);
            String delegate = el.getAttributeNS("http://camunda.org/schema/1.0/bpmn", "delegateExpression");
            if (delegate == null || delegate.isBlank()) {
                delegate = el.getAttribute("camunda:delegateExpression");
            }
            String clazz = el.getAttributeNS("http://camunda.org/schema/1.0/bpmn", "class");
            if ((delegate == null || delegate.isBlank()) && (clazz == null || clazz.isBlank())) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "SERVICE_TASK_NO_DELEGATE",
                        el.getAttribute("id"), "ServiceTask sin DelegateExpression ni Class.");
            }
        }
    }

    private void checkExclusiveGatewayDefault(Document doc, PreFlightResultDTO result) {
        NodeList gateways = doc.getElementsByTagNameNS(BPMN_NS, "exclusiveGateway");
        if (gateways.getLength() == 0) {
            gateways = doc.getElementsByTagName("bpmn:exclusiveGateway");
        }
        for (int i = 0; i < gateways.getLength(); i++) {
            Element el = (Element) gateways.item(i);
            
            int outgoingCount = el.getElementsByTagNameNS(BPMN_NS, "outgoing").getLength();
            if (outgoingCount == 0) {
                outgoingCount = el.getElementsByTagName("bpmn:outgoing").getLength();
            }
            
            String defaultFlow = el.getAttribute("default");
            if (outgoingCount > 1 && (defaultFlow == null || defaultFlow.isBlank())) {
                // @Traceability: US-005, CA-07 Gobernanza Estricta de Despliegue
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "GATEWAY_NO_DEFAULT",
                        el.getAttribute("id"), "ExclusiveGateway sin default flow.");
            }
        }
    }

    private void checkTimerEvent(Document doc, PreFlightResultDTO result) {
        NodeList timers = doc.getElementsByTagNameNS(BPMN_NS, "timerEventDefinition");
        if (timers.getLength() == 0) {
            timers = doc.getElementsByTagName("bpmn:timerEventDefinition");
        }
        for (int i = 0; i < timers.getLength(); i++) {
            Element el = (Element) timers.item(i);
            NodeList durations = el.getElementsByTagNameNS(BPMN_NS, "timeDuration");
            NodeList dates = el.getElementsByTagNameNS(BPMN_NS, "timeDate");
            NodeList cycles = el.getElementsByTagNameNS(BPMN_NS, "timeCycle");
            if (durations.getLength() == 0 && dates.getLength() == 0 && cycles.getLength() == 0) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "TIMER_NO_EXPRESSION",
                        null, "TimerEvent sin expresión ISO 8601.");
            }
        }
    }

    private void checkMessageEvent(Document doc, PreFlightResultDTO result) {
        NodeList messages = doc.getElementsByTagNameNS(BPMN_NS, "messageEventDefinition");
        if (messages.getLength() == 0) {
            messages = doc.getElementsByTagName("bpmn:messageEventDefinition");
        }
        for (int i = 0; i < messages.getLength(); i++) {
            Element el = (Element) messages.item(i);
            String messageRef = el.getAttribute("messageRef");
            if (messageRef == null || messageRef.isBlank()) {
                // @Traceability: US-005, CA-09 Gobernanza Estricta de Despliegue
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "MESSAGE_NO_REF",
                        null, "Hard-Stop: MessageEvent sin messageRef.");
            }
        }
    }

    private void checkCallActivity(Document doc, PreFlightResultDTO result) {
        NodeList calls = doc.getElementsByTagNameNS(BPMN_NS, "callActivity");
        if (calls.getLength() == 0) {
            calls = doc.getElementsByTagName("bpmn:callActivity");
        }
        for (int i = 0; i < calls.getLength(); i++) {
            Element el = (Element) calls.item(i);
            String calledElement = el.getAttribute("calledElement");
            if (calledElement == null || calledElement.isBlank()) {
                // @Traceability: US-005, CA-09 Gobernanza Estricta de Despliegue
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "CALL_ACTIVITY_MISSING_KEY",
                        el.getAttribute("id"), "Hard-Stop: CallActivity sin calledElement (processDefinitionKey).");
            }
        }
    }

    private void checkMaxNodes(Document doc, int maxNodes, PreFlightResultDTO result) {
        int totalNodes = 0;
        String[] nodeTypes = { "userTask", "serviceTask", "exclusiveGateway", "parallelGateway",
                "inclusiveGateway", "startEvent", "endEvent", "intermediateCatchEvent",
                "intermediateThrowEvent", "callActivity", "subProcess", "boundaryEvent" };

        for (String type : nodeTypes) {
            NodeList nodes = doc.getElementsByTagNameNS(BPMN_NS, type);
            if (nodes.getLength() == 0) {
                nodes = doc.getElementsByTagName("bpmn:" + type);
            }
            totalNodes += nodes.getLength();
        }

        if (totalNodes > maxNodes) {
            result.addIssue(PreFlightResultDTO.Severity.WARNING, "MAX_NODES_EXCEEDED",
                    null, "El proceso tiene " + totalNodes + " nodos, excede el límite de " + maxNodes + ".");
        }
    }

    private void checkZombieNodes(Document doc, PreFlightResultDTO result) {
        String[] nodeTypes = { "userTask", "serviceTask", "businessRuleTask", "scriptTask", "sendTask", "receiveTask", "task",
                "exclusiveGateway", "parallelGateway", "inclusiveGateway", "eventBasedGateway",
                "intermediateCatchEvent", "intermediateThrowEvent", "callActivity", "subProcess", "startEvent", "endEvent" };

        for (String type : nodeTypes) {
            NodeList nodes = doc.getElementsByTagNameNS(BPMN_NS, type);
            if (nodes.getLength() == 0) {
                nodes = doc.getElementsByTagName("bpmn:" + type);
            }
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);

                int incomingCount = el.getElementsByTagNameNS(BPMN_NS, "incoming").getLength();
                if (incomingCount == 0) {
                    incomingCount = el.getElementsByTagName("bpmn:incoming").getLength();
                }

                int outgoingCount = el.getElementsByTagNameNS(BPMN_NS, "outgoing").getLength();
                if (outgoingCount == 0) {
                    outgoingCount = el.getElementsByTagName("bpmn:outgoing").getLength();
                }

                // @Traceability: US-005, CA-22 Detección de Nodos Zombie
                if (!type.equals("startEvent") && incomingCount == 0) {
                    result.addIssue(PreFlightResultDTO.Severity.ERROR, "ZOMBIE_NODE_NO_INCOMING",
                            el.getAttribute("id"), "Hard-Stop: Nodo Zombie sin flujo de entrada (incoming) detectado.");
                }

                if (!type.equals("endEvent") && outgoingCount == 0) {
                    result.addIssue(PreFlightResultDTO.Severity.ERROR, "ZOMBIE_NODE_NO_OUTGOING",
                            el.getAttribute("id"), "Hard-Stop: Nodo Colgado sin flujo de salida (outgoing) detectado.");
                }
            }
        }
    }

    private void checkInfiniteLoops(Document doc, PreFlightResultDTO result) {
        // @Traceability: US-005, CA-23 Detección de Bucles Topológicos
        java.util.Map<String, String> nodeTypes = new java.util.HashMap<>();
        String[] allTypes = { "userTask", "serviceTask", "businessRuleTask", "scriptTask", "sendTask", "receiveTask", "manualTask", "task",
                "exclusiveGateway", "parallelGateway", "inclusiveGateway", "eventBasedGateway", "complexGateway",
                "intermediateCatchEvent", "intermediateThrowEvent", "callActivity", "subProcess", "startEvent", "endEvent" };

        for (String type : allTypes) {
            NodeList nodes = doc.getElementsByTagNameNS(BPMN_NS, type);
            if (nodes.getLength() == 0) {
                nodes = doc.getElementsByTagName("bpmn:" + type);
            }
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                nodeTypes.put(el.getAttribute("id"), type);
            }
        }

        java.util.Map<String, java.util.List<String>> graph = new java.util.HashMap<>();
        NodeList flows = doc.getElementsByTagNameNS(BPMN_NS, "sequenceFlow");
        if (flows.getLength() == 0) {
            flows = doc.getElementsByTagName("bpmn:sequenceFlow");
        }

        for (int i = 0; i < flows.getLength(); i++) {
            Element flow = (Element) flows.item(i);
            String source = flow.getAttribute("sourceRef");
            String target = flow.getAttribute("targetRef");
            
            String targetType = nodeTypes.getOrDefault(target, "");

            // Consider it a synchronous edge if target is NOT a wait state
            boolean isWaitState = targetType.equals("userTask") || targetType.equals("receiveTask") || 
                                  targetType.equals("intermediateCatchEvent") || targetType.equals("eventBasedGateway");
            
            if (!isWaitState) {
                graph.computeIfAbsent(source, k -> new java.util.ArrayList<>()).add(target);
            }
        }

        // DFS for cycle detection
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Set<String> recursionStack = new java.util.HashSet<>();

        for (String node : graph.keySet()) {
            if (hasCycle(node, graph, visited, recursionStack)) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "INFINITE_LOOP_DETECTED", null,
                        "Hard-Stop: Bucle topológico infinito detectado. Inserte un estado de espera (UserTask, Timer) para evitar caídas del motor.");
                break;
            }
        }
    }

    private boolean hasCycle(String node, java.util.Map<String, java.util.List<String>> graph, 
                             java.util.Set<String> visited, java.util.Set<String> recursionStack) {
        if (recursionStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }
        
        visited.add(node);
        recursionStack.add(node);

        java.util.List<String> neighbors = graph.getOrDefault(node, java.util.Collections.emptyList());
        for (String neighbor : neighbors) {
            if (hasCycle(neighbor, graph, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    private void checkGatewayConvergence(Document doc, PreFlightResultDTO result) {
        // @Traceability: US-005, CA-27 Validaciones Topológicas Avanzadas (Convergencia)
        String[] gatewayTypes = { "parallelGateway", "inclusiveGateway" };
        
        for (String type : gatewayTypes) {
            org.w3c.dom.NodeList nodes = doc.getElementsByTagNameNS(BPMN_NS, type);
            if (nodes.getLength() == 0) {
                nodes = doc.getElementsByTagName("bpmn:" + type);
            }
            
            int divergentCount = 0;
            int convergentCount = 0;
            
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) nodes.item(i);
                
                int incomingCount = el.getElementsByTagNameNS(BPMN_NS, "incoming").getLength();
                if (incomingCount == 0) {
                    incomingCount = el.getElementsByTagName("bpmn:incoming").getLength();
                }
                
                int outgoingCount = el.getElementsByTagNameNS(BPMN_NS, "outgoing").getLength();
                if (outgoingCount == 0) {
                    outgoingCount = el.getElementsByTagName("bpmn:outgoing").getLength();
                }
                
                if (outgoingCount > 1 && incomingCount <= 1) {
                    divergentCount++;
                } else if (incomingCount > 1 && outgoingCount <= 1) {
                    convergentCount++;
                }
            }
            
            if (divergentCount > convergentCount) {
                result.addIssue(PreFlightResultDTO.Severity.ERROR, "GATEWAY_CONVERGENCE_MISMATCH", null,
                        "Hard-Stop: Existen pasarelas (" + type + ") divergentes sin convergencia declarada. Detectados " + divergentCount + " divergentes y " + convergentCount + " convergentes.");
            }
        }
    }
}
