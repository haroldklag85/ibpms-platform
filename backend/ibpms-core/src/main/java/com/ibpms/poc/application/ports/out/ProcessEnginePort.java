package com.ibpms.poc.application.ports.out;

/**
 * Outbound Port para abstraer el motor de procesos (BPMN Workflow Engine).
 * Desacopla la capa de negocio de librerías propietarias como Camunda.
 * Requerimiento: US-003 (iForm Maestro - Formularios Dinámicos)
 */
public interface ProcessEnginePort {
    /**
     * Cuenta el número de instancias de proceso activas (no terminadas).
     * Requerido para validaciones de inmutabilidad CA-26.
     * @return Número de instancias activas.
     */
    long countActiveProcessInstances(String formKey);

    /**
     * Cuenta el número de tareas activas que potencialmente utilizan algún formulario.
     * Requerido para validar si es seguro aplicar soft-deletes (CA-26).
     * @return Número de tareas incompletas.
     */
    long countActiveTasksWithForm(String formKey);
}
