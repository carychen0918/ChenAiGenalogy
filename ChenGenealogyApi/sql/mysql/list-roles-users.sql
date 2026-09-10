SELECT id, name, code, type, status FROM system_role WHERE deleted = 0 ORDER BY id;
SELECT u.id, u.username, u.nickname, r.code, r.name
FROM system_users u
LEFT JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
LEFT JOIN system_role r ON r.id = ur.role_id AND r.deleted = 0
WHERE u.deleted = 0
ORDER BY u.id, r.id;
