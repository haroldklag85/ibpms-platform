package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleHierarchyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchyEntity, UUID> {

    List<RoleHierarchyEntity> findByParentRoleId(UUID parentRoleId);

    /**
     * US-036 CA-6: CTE Recursivo para resolver la herencia piramidal de roles.
     * Dado un role_id hijo, viaja hacia arriba por la tabla ibpms_sec_role_hierarchy
     * recolectando todos los parent_role_id ancestrales en la cadena jerárquica.
     *
     * Ejemplo: Analista(child) -> Coordinador(parent) -> Gerente(grandparent)
     *   Input: Analista.id → Output: [Coordinador.id, Gerente.id]
     */
    @Query(value = """
            WITH RECURSIVE role_tree AS (
                SELECT parent_role_id, child_role_id
                FROM ibpms_sec_role_hierarchy
                WHERE child_role_id = :roleId

                UNION ALL

                SELECT rh.parent_role_id, rh.child_role_id
                FROM ibpms_sec_role_hierarchy rh
                INNER JOIN role_tree rt ON rh.child_role_id = rt.parent_role_id
            )
            SELECT DISTINCT parent_role_id FROM role_tree
            """, nativeQuery = true)
    List<UUID> findAllAncestorRoleIds(@Param("roleId") UUID roleId);

    /**
     * Validación anti-ciclo: verifica si ya existe un path directo o inverso
     * entre dos roles en la jerarquía. Usado por el servicio antes de insertar.
     */
    boolean existsByParentRoleIdAndChildRoleId(UUID parentRoleId, UUID childRoleId);
}
