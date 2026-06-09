package com.ibpms.poc.infrastructure.jpa.repository.security;

import com.ibpms.poc.infrastructure.jpa.entity.security.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByName(String name);
    List<RoleEntity> findByIsVipRestrictedTrue();

    /**
     * CA-6 US-036 — Resolución de Árbol de Permisos por Herencia Piramidal.
     *
     * Recorre la jerarquía de roles hacia arriba (hijo → padre → abuelo…)
     * mediante una CTE recursiva PostgreSQL. Devuelve todos los UUID de roles
     * en el árbol para que el servicio aplane sus ProcessPermissions.
     *
     * El anclaje es el rol dado; el caso recursivo sigue la FK parent_role_id
     * hasta alcanzar un nodo sin padre (NULL), momento en que la recursión termina.
     */
    @Query(value = """
            WITH RECURSIVE role_tree AS (
                SELECT id, parent_role_id
                FROM ibpms_security_role
                WHERE id = :roleId
                UNION ALL
                SELECT r.id, r.parent_role_id
                FROM ibpms_security_role r
                INNER JOIN role_tree rt ON r.id = rt.parent_role_id
            )
            SELECT id FROM role_tree
            """, nativeQuery = true)
    List<UUID> findRoleIdsInTree(@Param("roleId") UUID roleId);

    @Query(value = """
            WITH RECURSIVE role_tree AS (
                SELECT id, parent_role_id, name
                FROM ibpms_security_role
                WHERE name = :name
                UNION ALL
                SELECT r.id, r.parent_role_id, r.name
                FROM ibpms_security_role r
                INNER JOIN role_tree rt ON r.id = rt.parent_role_id
            )
            SELECT name FROM role_tree
            """, nativeQuery = true)
    List<String> findInheritedRoleNamesByName(@Param("name") String name);
}
