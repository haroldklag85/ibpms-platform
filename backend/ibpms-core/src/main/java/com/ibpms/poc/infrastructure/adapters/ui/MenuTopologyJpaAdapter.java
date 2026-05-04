package com.ibpms.poc.infrastructure.adapters.ui;

import com.ibpms.poc.application.dto.ui.MenuItemDTO;
import com.ibpms.poc.application.ports.out.MenuTopologyPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MenuTopologyJpaAdapter implements MenuTopologyPort {

    private final JdbcTemplate jdbcTemplate;

    public MenuTopologyJpaAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MenuItemDTO> findMenuTreeByRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder whereClause = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int i = 0;
        for (String role : roles) {
            if (i > 0) {
                whereClause.append(" OR ");
            }
            whereClause.append("required_roles @> ?::jsonb");
            params.add("[\"" + role + "\"]");
            i++;
        }

        // Suponemos que parent_id nulo es la raíz. Ordenamos por parent_id NULLS FIRST y luego por id
        String sql = "SELECT id, parent_id, label, icon, path, sort_order FROM ibpms_menu_topology " +
                     "WHERE " + whereClause.toString() + " ORDER BY parent_id NULLS FIRST, sort_order ASC, id";

        List<MenuRecord> records = jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            MenuRecord record = new MenuRecord();
            record.id = rs.getObject("id", UUID.class);
            record.parentId = rs.getObject("parent_id", UUID.class);
            record.label = rs.getString("label");
            record.icon = rs.getString("icon");
            record.path = rs.getString("path");
            return record;
        });

        Map<UUID, MenuItemDTO> nodeMap = new LinkedHashMap<>();
        List<MenuItemDTO> rootNodes = new ArrayList<>();

        for (MenuRecord rec : records) {
            MenuItemDTO item = new MenuItemDTO(rec.label, rec.icon, rec.path);
            nodeMap.put(rec.id, item);
        }

        for (MenuRecord rec : records) {
            if (rec.parentId == null || !nodeMap.containsKey(rec.parentId)) {
                rootNodes.add(nodeMap.get(rec.id));
            } else {
                MenuItemDTO parentNode = nodeMap.get(rec.parentId);
                parentNode.addChild(nodeMap.get(rec.id));
            }
        }

        return rootNodes;
    }

    private static class MenuRecord {
        UUID id;
        UUID parentId;
        String label;
        String icon;
        String path;
    }
}
