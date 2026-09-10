SET NAMES utf8mb4;

-- 字辈派语菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6806, '字辈派语', 'genealogy:content:query', 2, 4, 6802, 'generation', 'ep:collection-tag', 'genealogy/generation/index', 'GenealogyGeneration', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 6806);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6906, '字辈维护', 'genealogy:content:update', 3, 1, 6806, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 6906);

-- 授权给族长、管理员、省市县管理员
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.role_id, m.menu_id, '1', NOW(), '1', NOW(), b'0', 1
FROM (
  SELECT 201 AS role_id UNION ALL SELECT 202 UNION ALL SELECT 204 UNION ALL SELECT 205 UNION ALL SELECT 206
) r
CROSS JOIN (
  SELECT 6806 AS menu_id UNION ALL SELECT 6906
) m
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id AND rm.deleted = 0
);

-- 演示：四世并行「万」字辈
INSERT INTO `tb_generation` (`family_id`, `generation_no`, `word`, `status`, `remark`, `sort`, `tenant_id`, `creator`)
SELECT 1, 4, '万', 3, '同一世并行字辈示例', 41, 1, '1'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `tb_generation` WHERE `family_id` = 1 AND `generation_no` = 4 AND `word` = '万' AND `deleted` = 0
);
