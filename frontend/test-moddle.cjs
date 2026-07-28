const BpmnModdle = require('bpmn-moddle');
const camundaPackage = require('camunda-bpmn-moddle/resources/camunda.json');

const moddle = new BpmnModdle({ camunda: camundaPackage });
const xml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
  id="Definitions_1">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" camunda:formKey="123" />
  </bpmn:process>
</bpmn:definitions>`;

moddle.fromXML(xml).then(result => {
  console.log('SUCCESS', result.rootElement.id);
}).catch(err => {
  console.error('ERROR', err);
});
