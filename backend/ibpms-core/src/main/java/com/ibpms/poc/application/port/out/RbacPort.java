package com.ibpms.poc.application.port.out;

import java.util.List;

public interface RbacPort {
    /**
     * Guarda y asocia un rol autogenerado al Carril (Lane) de un Proceso.
     * Si el perfil y la asignación ya existen, no hace nada o la actualiza.
     */
    void bindLaneToProfile(String processKey, String laneId, String profileName, String description);

    /**
     * Devuelve una lista de Lanes BPMN asumiendo los roles de sistema indicados.
     */
    List<String> getPermittedBpmnLanesForGroups(List<String> idpGroupsList);
}
