package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.entity.IbpmsRoleEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsRoleRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.xml.sax.InputSource;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pre-Flight Analyzer — CA-9, CA-24.
 * Parsea el XML BPMN del borrador y valida reglas estructurales.
 */
@Service
public class PreFlightAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(PreFlightAnalyzerService.class);

    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    private final BpmnDesignService designService;
    private final BpmnDesignAuditLogRepository auditRepository;
    private final IbpmsRoleRepository roleRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.ExternalTaskTopicRepository externalTaskTopicRepository;

    public PreFlightAnalyzerService(BpmnDesignService designService,
            BpmnDesignAuditLogRepository auditRepository,
            IbpmsRoleRepository roleRepository,
            com.ibpms.poc.infrastructure.jpa.repository.ExternalTaskTopicRepository externalTaskTopicRepository) {
        this.designService = designService;
        this.auditRepository = auditRepository;
        this.roleRepository = roleRepository;
        this.externalTaskTopicRepository = externalTaskTopicRepository;
    }

    public PreFlightResultDTO analizar(java.util.UUID processDesignId, String userId) {
        BpmnProcessDesignEntity entity = designService.findOrFail(processDesignId);
        String xml = entity.getXmlDraft();

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("No hay borrador XML para analizar.");
        }

        PreFlightResultDTO result = new PreFlightResultDTO();

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            // Regla 1: UserTask sin camunda:formKey → ERROR
            checkUserTaskFormKey(doc, result);

            // Regla 2: ServiceTask sin delegateExpression ni conectorHub → ERROR
            checkServiceTaskDelegate(doc, result);

            // Regla 3: ExclusiveGateway sin default flow → ERROR
            checkExclusiveGatewayDefault(doc, result);

            // Regla 4: TimerEvent sin expresión ISO 8601 → ERROR
            checkTimerEvent(doc, result);

            // Regla 5: MessageEvent sin messageRef → WARNING
            checkMessageEvent(doc, result);

            // Regla 6: CallActivity con processDefinitionKey inexistente → WARNING
            checkCallActivity(doc, result);

            // Regla 7: Nodos totales > maxNodes → WARNING
            checkMaxNodes(doc, entity.getMaxNodes(), result);

        } catch (Exception e) {
            result.addIssue(PreFlightResultDTO.Severity.ERROR, "XML_PARSE",
                    null, "Error parseando el XML BPMN: " + e.getMessage());
        }

        boolean hasErrors = result.getIssues().stream()
                .anyMatch(i -> "ERROR".equals(i.getSeverity()));
        result.setPassed(!hasErrors);

        // Audit log
        auditRepository.save(new BpmnDesignAuditLogEntity(
                processDesignId, BpmnDesignAuditLogEntity.Action.PRE_FLIGHT, userId,
                entity.getCurrentVersion(),
                "{\"passed\":" + result.isPassed() + ",\"issues\":" + result.getIssues().size() + "}"));

        return result;
    }

    /**
     * CA-1 a CA-4: Análisis semántico en caliente usando Camunda BPMN Model API.
     */
    public DeploymentValidationResponse analizar(InputStream bpmnStream) {
        DeploymentValidationResponse response = new DeploymentValidationResponse();
        response.setValid(true);

        try {
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(bpmnStream);

            // CA-6 VIP Pre-allocation mapping
            List<String> vipRoleNames = roleRepository.findByIsVipRestrictedTrue().stream()
                .map(IbpmsRoleEntity::getName).map(String::toUpperCase).collect(Collectors.toList());

            // CA-2: Control de diagrama roto (Falta End Event)
            Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
            if (endEvents == null || endEvents.isEmpty()) {
                response.addError("Diagram", "Falta End Event");
            }

            // CA-3.1 y CA-70: ServiceTask requiere delegación y topic válido
            List<String> activeTopics = externalTaskTopicRepository.findByIsActiveTrue().stream()
                .map(com.ibpms.poc.infrastructure.jpa.entity.ExternalTaskTopicEntity::getTopicName)
                .collect(Collectors.toList());

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
                    // CA-70: Validar topic contra el catálogo
                    if (!activeTopics.contains(cmdTopic)) {
                        response.addError(st.getId(), "Topic '" + cmdTopic + "' no está registrado o inactivo en el catálogo de External Tasks.");
                    }
                }
            }

            // CA-3.2: UserTask requiere formKey y CA-6 verificación de form genérico en Lane VIP
            Collection<UserTask> userTasks = modelInstance.getModelElementsByType(UserTask.class);
            Collection<Lane> lanes = modelInstance.getModelElementsByType(Lane.class);
            for (UserTask ut : userTasks) {
                String formKey = ut.getCamundaFormKey();
                if (formKey == null || formKey.isBlank()) {
                    response.addError(ut.getId(), "UserTask carece de camunda:formKey obligatorio");
                } else if ("sys_generic_form".equals(formKey)) {
                    // Verificamos en qué Lane se encuentra
                    for (Lane lane : lanes) {
                        if (lane.getFlowNodeRefs().contains(ut)) {
                            // Construir el rol derivado del lane mapping as per CA-6 (o simplemente verificar si el nombre del lane contiene un rol VIP)
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

            // CA-3.3: ExclusiveGateway con advertencia sobre default flow
            Collection<ExclusiveGateway> gateways = modelInstance.getModelElementsByType(ExclusiveGateway.class);
            for (ExclusiveGateway gw : gateways) {
                if (gw.getDefault() == null) {
                    response.addWarning(gw.getId(), "ExclusiveGateway sin Flujo por Defecto (default property)");
                }
            }

            // CA-4: StartEvent requiere formKey obligatorio
            Collection<StartEvent> startEvents = modelInstance.getModelElementsByType(StartEvent.class);
            boolean hasValidStartForm = false;
            for (StartEvent se : startEvents) {
                if (se.getCamundaFormKey() != null && !se.getCamundaFormKey().isBlank()) {
                    hasValidStartForm = true;
                    break;
                }
            }
            if (!hasValidStartForm && !startEvents.isEmpty()) {
                response.addError("StartEvent", "El StartEvent carece de camunda:formKey obligatorio para iniciar instancia de forma manual");
            }

            // CA-12: Versionamiento Seguro de Reglas DMN (Protección de Derechos Adquiridos)
            Collection<BusinessRuleTask> brTasks = modelInstance.getModelElementsByType(BusinessRuleTask.class);
            for (BusinessRuleTask brt : brTasks) {
                String decisionRef = brt.getCamundaDecisionRef();
                String binding = brt.getCamundaDecisionRefBinding();

                if (decisionRef != null && !decisionRef.isBlank()) {
                    if (binding == null || binding.isBlank()) {
                        response.addWarning(brt.getId(),
                            "⚠️ BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                            "' enlaza a DMN (" + decisionRef + ") sin camunda:decisionRefBinding configurado. " +
                            "El motor asumirá 'latest' por defecto, lo cual puede violar la protección de derechos adquiridos (CA-12). " +
                            "Recomendación: Configure 'deployment' en el Modeler para garantizar que los casos en vuelo se evalúen con la versión DMN vigente al nacer el caso.");
                    } else if ("latest".equals(binding)) {
                        response.addWarning(brt.getId(),
                            "ℹ️ BusinessRuleTask '" + (brt.getName() != null ? brt.getName() : brt.getId()) +
                            "' usa Late Binding (LATEST). Los casos en vuelo se evaluarán con la última versión DMN publicada. " +
                            "Confirme que este comportamiento es intencional y no viola compromisos contractuales.");
                    }
                    // binding="deployment" → OK silencioso (default seguro)
                }
            }

            // CA-5: Nomenclatura Obligatoria de Instancia
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
            if (!hasNomenclature) {
                response.addError("Process", "Debe definir cómo se llamarán los casos de este proceso (Propiedad: ReglaNomenclatura).");
            }

            // CA-18.1: TimerEvent requiere configuracion (timeDuration o timeCycle)
            Collection<TimerEventDefinition> timers = modelInstance.getModelElementsByType(TimerEventDefinition.class);
            for (TimerEventDefinition t : timers) {
                if ((t.getTimeDuration() == null || t.getTimeDuration().getTextContent().isBlank()) &&
                    (t.getTimeCycle() == null || t.getTimeCycle().getTextContent().isBlank()) &&
                    (t.getTimeDate() == null || t.getTimeDate().getTextContent().isBlank())) {
                    response.addError(t.getId() != null ? t.getId() : "TimerEvent", "TimerEvent sin expresión de tiempo válida definida.");
                }
            }

            // CA-18.2 y CA-46: MessageEvent requiere messageRef y conector
            Collection<MessageEventDefinition> messages = modelInstance.getModelElementsByType(MessageEventDefinition.class);
            for (MessageEventDefinition m : messages) {
                if (m.getMessage() == null) {
                    response.addError(m.getId() != null ? m.getId() : "MessageEvent", "MessageEvent carece de Message Reference (messageRef).");
                }
                
                // CA-46: Detección de Eventos Ciegos (Sin delegateExpression o conector)
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

            // CA-18.3: CallActivity requiere calledElement
            Collection<CallActivity> calls = modelInstance.getModelElementsByType(CallActivity.class);
            for (CallActivity ca : calls) {
                if (ca.getCalledElement() == null || ca.getCalledElement().isBlank()) {
                    response.addError(ca.getId(), "CallActivity invoca subproceso pero carece de la propiedad 'calledElement'.");
                }
            }

            // CA-6: Autogeneración de Roles RBAC desde Lanes (Si el proceso aprueba o incluso si falla el log lo reporta al final)
            // Se calcula proyectivamente.
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

    // --- Reglas de Validación ---

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
            String defaultFlow = el.getAttribute("default");
            // Solo aplica a gateways divergentes (con más de una salida)
            if (defaultFlow == null || defaultFlow.isBlank()) {
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
                result.addIssue(PreFlightResultDTO.Severity.WARNING, "MESSAGE_NO_REF",
                        null, "MessageEvent sin messageRef.");
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
                result.addIssue(PreFlightResultDTO.Severity.WARNING, "CALL_ACTIVITY_MISSING_KEY",
                        el.getAttribute("id"), "CallActivity sin calledElement (processDefinitionKey).");
            }
        }
    }

    private void checkMaxNodes(Document doc, int maxNodes, PreFlightResultDTO result) {
        // Contar todos los elementos BPMN principales
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
}
