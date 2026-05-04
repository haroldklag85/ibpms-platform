SELECT COUNT(*) FROM ibpms_menu_topology;
SELECT label FROM ibpms_menu_topology WHERE parent_id IS NULL ORDER BY sort_order;
SELECT label FROM ibpms_menu_topology WHERE required_roles @> '["ROLE_CISO"]';
