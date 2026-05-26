INSERT INTO ibpms_bpmn_process_design (
    id, technical_id, name, current_version, xml_draft, generic_form_whitelist, status, created_at, updated_at, created_by
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000', 
    'PRC-CREDIT-001', 
    'Proceso de Evaluacion de Credito', 
    1, 
    '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_Credit_001" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1"/>
  </bpmn:process>
</bpmn:definitions>', 
    '["ingreso_solicitud", "evaluacion_riesgo", "aprobacion_final"]', 
    'PUBLISHED', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    'sysadmin'
);
