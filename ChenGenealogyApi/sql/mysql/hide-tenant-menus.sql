UPDATE system_menu SET status = 1, visible = b'0'
WHERE id IN (1138, 1139, 1140, 1141, 1142, 1143, 1224, 1225, 1226, 1227, 1228, 1229, 5010);

SELECT id, name, path, status, visible FROM system_menu WHERE id IN (1138, 1224, 1225, 5010);
