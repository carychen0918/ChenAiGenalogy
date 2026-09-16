-- 二期客厅配置：仅新建表，不更新既有业务数据
CREATE TABLE IF NOT EXISTS `tb_showcase_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL COMMENT '家族编号',
  `featured_member_id` bigint DEFAULT NULL COMMENT '首页置顶人物，空则自动选取',
  `featured_deed_id` bigint DEFAULT NULL COMMENT '置顶成员事迹 tb_member_deed.id',
  `featured_ancestor_deed_id` bigint DEFAULT NULL COMMENT '置顶祖先事迹',
  `wall_application_ids` varchar(2000) DEFAULT NULL COMMENT '资助榜样申请 ID JSON，空则自动取已发放',
  `calendar_member_ids` varchar(2000) DEFAULT NULL COMMENT '家族日历展示成员 ID JSON，空则不展示寿辰忌日',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family` (`family_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='族谱客厅展示配置';

-- 已建表环境补列；若提示 Duplicate column 说明列已存在，可忽略
ALTER TABLE `tb_showcase_config`
  ADD COLUMN `calendar_member_ids` varchar(2000) DEFAULT NULL COMMENT '家族日历展示成员 ID JSON，空则不展示寿辰忌日';
