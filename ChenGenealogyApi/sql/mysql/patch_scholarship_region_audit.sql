-- 成员地区、地区管理员绑定、助学逐级审核

ALTER TABLE `tb_member`
  ADD COLUMN `province_id` int DEFAULT NULL COMMENT '省' AFTER `alive`,
  ADD COLUMN `city_id` int DEFAULT NULL COMMENT '市' AFTER `province_id`,
  ADD COLUMN `county_id` int DEFAULT NULL COMMENT '县/区' AFTER `city_id`,
  ADD COLUMN `address` varchar(255) DEFAULT NULL COMMENT '详细地址' AFTER `county_id`;

CREATE TABLE IF NOT EXISTS `tb_admin_region` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '系统用户',
  `audit_level` varchar(16) NOT NULL COMMENT 'county/city/province',
  `area_id` int NOT NULL COMMENT '管辖地区编号',
  `province_id` int DEFAULT NULL,
  `city_id` int DEFAULT NULL,
  `county_id` int DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_level` (`user_id`, `audit_level`),
  KEY `idx_level_area` (`audit_level`, `area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区管理员管辖范围';

ALTER TABLE `tb_scholarship_application`
  ADD COLUMN `province_id` int DEFAULT NULL COMMENT '申请时省' AFTER `submit_time`,
  ADD COLUMN `city_id` int DEFAULT NULL COMMENT '申请时市' AFTER `province_id`,
  ADD COLUMN `county_id` int DEFAULT NULL COMMENT '申请时县' AFTER `city_id`,
  ADD COLUMN `region_name` varchar(128) DEFAULT NULL COMMENT '申请时地区名称' AFTER `county_id`,
  ADD COLUMN `current_audit_level` varchar(16) DEFAULT NULL COMMENT '当前待审级别' AFTER `region_name`,
  ADD COLUMN `audit_pipeline` varchar(128) DEFAULT NULL COMMENT '审核链路 JSON' AFTER `current_audit_level`;

CREATE TABLE IF NOT EXISTS `tb_scholarship_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `audit_level` varchar(16) NOT NULL COMMENT 'county/city/province/family',
  `result` tinyint NOT NULL COMMENT '1通过 2驳回 3补材料',
  `opinion` varchar(500) DEFAULT NULL,
  `auditor_user_id` bigint DEFAULT NULL,
  `auditor_name` varchar(64) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助审核记录';

-- 字典：逐级审核状态
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 10, '待县审', '10', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='genealogy_scholarship_status' AND value='10' AND deleted=b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 11, '待市审', '11', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='genealogy_scholarship_status' AND value='11' AND deleted=b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 12, '待省审', '12', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='genealogy_scholarship_status' AND value='12' AND deleted=b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 13, '待家族审', '13', 'genealogy_scholarship_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type='genealogy_scholarship_status' AND value='13' AND deleted=b'0');

UPDATE `system_dict_data` SET `label`='待初审(旧)' WHERE `dict_type`='genealogy_scholarship_status' AND `value`='1' AND `deleted`=b'0';
UPDATE `system_dict_data` SET `label`='初审通过(旧)' WHERE `dict_type`='genealogy_scholarship_status' AND `value`='3' AND `deleted`=b'0';

-- 历史待初审 → 待家族审；初审通过 → 待发放
UPDATE `tb_scholarship_application` SET `status`=13, `current_audit_level`='family', `audit_pipeline`='["family"]' WHERE `status`=1;
UPDATE `tb_scholarship_application` SET `status`=4 WHERE `status`=3;

-- 演示成员默认落到贵阳乌当，便于按地区分派
UPDATE `tb_member` SET `province_id`=520000, `city_id`=520100, `county_id`=520112, `address`='乌当区' WHERE `deleted`=b'0' AND `province_id` IS NULL;

-- 菜单：地区管理员
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6807, '地区管理员', 'genealogy:admin-region:query', 2, 5, 6802, 'region-admin', 'ep:location', 'genealogy/region-admin/index', 'GenealogyRegionAdmin', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE id=6807);
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 6908, '地区绑定', 'genealogy:admin-region:update', 3, 1, 6807, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM system_menu WHERE id=6908);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 201, 6807, '1', NOW(), '1', NOW(), b'0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=201 AND menu_id=6807 AND deleted=b'0');
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 201, 6908, '1', NOW(), '1', NOW(), b'0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=201 AND menu_id=6908 AND deleted=b'0');
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 202, 6807, '1', NOW(), '1', NOW(), b'0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=202 AND menu_id=6807 AND deleted=b'0');
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 202, 6908, '1', NOW(), '1', NOW(), b'0', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_role_menu WHERE role_id=202 AND menu_id=6908 AND deleted=b'0');
