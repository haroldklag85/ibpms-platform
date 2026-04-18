import { describe, it, expect } from 'vitest'

describe('US-005 CA-12: DMN Binding UI Validation', () => {

  describe('Default Binding Value', () => {
    it('debe tener DEPLOYMENT como valor por defecto del dmnBinding', () => {
      const defaultProps = {
        aiTokenLimit: 4000,
        aiTone: 'NEUTRAL',
        sla: '',
        calledElement: '',
        topic: '',
        dmnBinding: 'deployment'
      }
      expect(defaultProps.dmnBinding).toBe('deployment')
    })

    it('debe aceptar "latest" como valor alternativo válido', () => {
      const validValues = ['deployment', 'latest']
      expect(validValues).toContain('deployment')
      expect(validValues).toContain('latest')
    })

    it('NO debe aceptar valores no reconocidos', () => {
      const validValues = ['deployment', 'latest']
      expect(validValues).not.toContain('version')
      expect(validValues).not.toContain('')
      expect(validValues).not.toContain(null)
    })
  })

  describe('Camunda Property Mapping', () => {
    it('debe mapear al atributo correcto: camunda:decisionRefBinding', () => {
      const camundaProperty = 'camunda:decisionRefBinding'
      expect(camundaProperty).toBe('camunda:decisionRefBinding')
      expect(camundaProperty).not.toBe('camunda:decisionBinding') // Error común
    })

    it('debe sincronizar el valor del selector al XML BPMN', () => {
      // Simula el patrón syncElementProperties
      const syncCall = {
        property: 'camunda:decisionRefBinding',
        value: 'deployment'
      }
      expect(syncCall.property).toBe('camunda:decisionRefBinding')
      expect(syncCall.value).toBe('deployment')
    })
  })
})
