package com.ibpms.poc.application.service;

import com.ibpms.poc.application.port.in.AuditLogUseCase;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService implements AuditLogUseCase {

    private final Javers javers;

    public AuditLogService(Javers javers) {
        this.javers = javers;
    }

    @Override
    public String getEntityDiffHistory(String entityType, String entityId) {
        // En este bloque traducimos el String genérico al Class correspondiente
        Class<?> clazz;
        try {
            clazz = Class.forName("com.ibpms.poc.infrastructure.jpa.entity." + entityType);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Entidad desconocida para auditoría: " + entityType);
        }

        // Javers nos extrae un objeto estructurado Changes (Lista de ValueChange o
        // ReferenceChange)
        QueryBuilder jqlQuery = QueryBuilder.byInstanceId(entityId, clazz);
        Changes changes = javers.findChanges(jqlQuery.build());

        // Se retorna el JSON oficial serializado de Javers
        return javers.getJsonConverter().toJson(changes);
    }
}
