package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.PreFlightResultDTO;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnDesignAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.entity.BpmnProcessDesignEntity;
import com.ibpms.poc.infrastructure.jpa.repository.BpmnDesignAuditLogRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;

import com.ibpms.poc.application.dto.DeploymentValidationResponse;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
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

    public PreFlightAnalyzerService(BpmnDesignService designService,
            BpmnDesignAuditLogRepository auditRepository) {
        this.designService = designService;
        this.auditRepository = auditRepository;
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

            // CA-2: Control de diagrama roto (Falta End Event)
            Collection<EndEvent> endEvents = modelInstance.getModelElementsByType(EndEvent.class);
            if (endEvents == null || endEvents.isEmpty()) {
                response.addError("Diagram", "Falta End Event");
            }

            // CA-3.1: ServiceTask requiere delegación
            Collection<ServiceTask> serviceTasks = modelInstance.getModelElementsByType(ServiceTask.class);
            for (ServiceTask st : serviceTasks) {
                String cmdDelExpr = st.getCamundaDelegateExpression();
                String cmdClass = st.getCamundaClass();
                String cmdTopic = st.getCamundaTopic();
                if ((cmdDelExpr == null || cmdDelExpr.isBlank()) &&
                    (cmdClass == null || cmdClass.isBlank()) &&
                    (cmdTopic == null || cmdTopic.isBlank())) {
                    response.addError(st.getId(), "ServiceTask carece de propiedad de ejecución (delegateExpression, class o topic)");
                }
            }

            // CA-3.2: UserTask requiere formKey
            Collection<UserTask> userTasks = modelInstance.getModelElementsByType(UserTask.class);
            for (UserTask ut : userTasks) {
                if (ut.getCamundaFormKey() == null || ut.getCamundaFormKey().isBlank()) {
                    response.addError(ut.getId(), "UserTask carece de camunda:formKey obligatorio");
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

            // CA-6: Autogeneración de Roles RBAC desde Lanes (Si el proceso aprueba o incluso si falla el log lo reporta al final)
            // Se calcula proyectivamente.
            Collection<Lane> lanes = modelInstance.getModelElementsByType(Lane.class);
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
