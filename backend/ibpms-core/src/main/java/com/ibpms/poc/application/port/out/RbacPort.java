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

    /**
     * Purga las asignaciones de carriles (y perfiles opcionalmente) que ya no
     * existen en el proceso BPMN.
     * @Traceability: US-005, CA-06 Purga de Roles Zombies
     */
    void purgeZombieLanes(String processKey, List<String> activeLaneIds);
}
