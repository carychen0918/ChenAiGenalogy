-- 字辈派语对照表：高安椒坊字派 + 三房独立于三房织金
-- 所属房改为：1长房 2二房 3三房 4三房织金 5四五房

ALTER TABLE `tb_generation`
  ADD COLUMN `jiaofang_source` varchar(16) DEFAULT NULL COMMENT '高安椒坊字派' AFTER `national_source`;

UPDATE `system_dict_type`
SET `remark` = '长房、二房、三房、三房织金、四五房'
WHERE `type` = 'genealogy_generation_house' AND `deleted` = b'0';

UPDATE `system_dict_data`
SET `value` = '5', `sort` = 5
WHERE `dict_type` = 'genealogy_generation_house' AND `value` = '4' AND `deleted` = b'0';

UPDATE `system_dict_data`
SET `value` = '4', `sort` = 4
WHERE `dict_type` = 'genealogy_generation_house' AND `value` = '3' AND `label` = '三房织金' AND `deleted` = b'0';

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 3, '三房', '3', 'genealogy_generation_house', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data` d WHERE d.`dict_type` = 'genealogy_generation_house' AND d.`value` = '3' AND d.`deleted` = b'0'
);

UPDATE `tb_generation` SET `house` = '5' WHERE `house` = '4' AND `deleted` = b'0';
UPDATE `tb_generation` SET `house` = '4' WHERE `house` = '3' AND `deleted` = b'0';
