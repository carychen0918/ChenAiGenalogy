SELECT id, name, path, component, parent_id, status, visible FROM system_menu WHERE deleted = 0 AND (path LIKE '%tenant%' OR component LIKE '%tenant%' OR name LIKE '%租户%');
