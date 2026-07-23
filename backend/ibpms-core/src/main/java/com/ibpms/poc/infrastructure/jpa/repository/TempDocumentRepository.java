package com.ibpms.poc.infrastructure.jpa.repository;

import com.ibpms.poc.infrastructure.jpa.entity.TempDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
public interface TempDocumentRepository extends JpaRepository<TempDocumentEntity, UUID> {

    /**
     * CA-17: Elimina documentos temporales orphaned (status indicado) subidos antes del cutoff,
     * excluyendo aquellos cuya tarea asociada está actualmente CLAIMED (operario trabajando).
     *
     * @param status     Estado del documento (e.g., "UPLOADED" = no confirmado = orphaned)
     * @param cutoffTime Timestamp límite — archivos subidos antes serán eliminados
     * @return número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM TempDocumentEntity d WHERE d.status = :status AND d.uploadedAt < :cutoffTime " +
           "AND (d.taskId IS NULL OR d.taskId NOT IN " +
           "(SELECT CAST(t.id AS string) FROM AgileTaskJpaEntity t WHERE t.status = 'CLAIMED'))")
    int deleteByStatusAndUploadedAtBefore(@Param("status") String status, @Param("cutoffTime") ZonedDateTime cutoffTime);
}
