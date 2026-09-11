-- 字辈：全国统一字源 + 所属房
-- 已有库执行本脚本即可，无需重建

ALTER TABLE `tb_generation`
  ADD COLUMN `national_source` varchar(16) DEFAULT NULL COMMENT '全国统一字源' AFTER `word`,
  ADD COLUMN `house` varchar(16) DEFAULT NULL COMMENT '所属房：1长房 2二房 3三房织金 4四五房' AFTER `national_source`;

UPDATE `tb_generation`
SET `national_source` = `word`
WHERE `national_source` IS NULL AND `word` IS NOT NULL AND `deleted` = b'0';

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '全国统一字源', 'genealogy_generation_source', 0, '陈氏全国统一字辈字源，可在字典管理中增补', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'genealogy_generation_source' AND `deleted` = b'0');

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT '所属房', 'genealogy_generation_house', 0, '长房、二房、三房织金、四五房', '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'genealogy_generation_house' AND `deleted` = b'0');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
  SELECT 1 AS sort, '永' AS label, '永' AS value, 'genealogy_generation_source' AS dict_type, 0 AS status, '' AS color_type, '' AS css_class, NULL AS remark, '1' AS creator, NOW() AS create_time, '1' AS updater, NOW() AS update_time, b'0' AS deleted
  UNION ALL SELECT 2, '同', '同', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 3, '昌', '昌', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 4, '世', '世', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 5, '德', '德', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 6, '万', '万', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 7, '文', '文', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 8, '明', '明', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 9, '绍', '绍', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 10, '启', '启', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 11, '家', '家', 'genealogy_generation_source', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data` d WHERE d.`dict_type` = t.dict_type AND d.`value` = t.value AND d.`deleted` = b'0'
);

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT * FROM (
  SELECT 1 AS sort, '长房' AS label, '1' AS value, 'genealogy_generation_house' AS dict_type, 0 AS status, 'danger' AS color_type, '' AS css_class, NULL AS remark, '1' AS creator, NOW() AS create_time, '1' AS updater, NOW() AS update_time, b'0' AS deleted
  UNION ALL SELECT 2, '二房', '2', 'genealogy_generation_house', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 3, '三房织金', '3', 'genealogy_generation_house', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'
  UNION ALL SELECT 4, '四五房', '4', 'genealogy_generation_house', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data` d WHERE d.`dict_type` = t.dict_type AND d.`value` = t.value AND d.`deleted` = b'0'
);
