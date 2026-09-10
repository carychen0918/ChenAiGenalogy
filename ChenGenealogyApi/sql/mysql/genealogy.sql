-- 陈氏族谱系统初始化脚本（MySQL 5.7+）
-- 业务表统一使用 tb_ 前缀

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===================== 业务表 =====================

DROP TABLE IF EXISTS `tb_family`;
CREATE TABLE `tb_family` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(64) NOT NULL COMMENT '家族名称',
  `surname` varchar(16) NOT NULL COMMENT '姓氏',
  `ancestor_name` varchar(32) DEFAULT NULL COMMENT '始祖姓名',
  `ancestor_id` bigint DEFAULT NULL COMMENT '始祖成员编号',
  `region` varchar(64) DEFAULT NULL COMMENT '所在地区',
  `intro` varchar(2000) DEFAULT NULL COMMENT '家族简介',
  `origin_content` mediumtext COMMENT '姓氏源流富文本',
  `book_title` varchar(64) DEFAULT NULL COMMENT '谱书名称',
  `book_revision` varchar(32) DEFAULT NULL COMMENT '重修记',
  `book_preface` mediumtext COMMENT '谱书前言/谱序富文本',
  `patriarch_user_id` bigint DEFAULT NULL COMMENT '族长用户编号',
  `logo` varchar(512) DEFAULT NULL COMMENT 'Logo',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家族信息';

DROP TABLE IF EXISTS `tb_generation`;
CREATE TABLE `tb_generation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `family_id` bigint NOT NULL COMMENT '家族编号',
  `generation_no` int NOT NULL COMMENT '世代数',
  `word` varchar(8) NOT NULL COMMENT '字辈文字',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1在用 2已用 3备用',
  `remark` varchar(255) DEFAULT NULL COMMENT '说明',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_family_gen` (`family_id`, `generation_no`),
  KEY `idx_family_gen_word` (`family_id`, `generation_no`, `word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字辈排行（同一世可有多个字辈）';

DROP TABLE IF EXISTS `tb_member`;
CREATE TABLE `tb_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员编号',
  `family_id` bigint NOT NULL COMMENT '家族编号',
  `user_id` bigint DEFAULT NULL COMMENT '关联系统用户',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `gender` tinyint NOT NULL COMMENT '性别：1男 2女',
  `generation_id` bigint DEFAULT NULL COMMENT '字辈编号',
  `generation_no` int DEFAULT NULL COMMENT '世代数',
  `birth_date` datetime DEFAULT NULL COMMENT '出生日期',
  `death_date` datetime DEFAULT NULL COMMENT '逝世日期',
  `father_id` bigint DEFAULT NULL COMMENT '父亲编号',
  `mother_id` bigint DEFAULT NULL COMMENT '母亲编号',
  `spouse_ids` varchar(512) DEFAULT NULL COMMENT '配偶编号 JSON 数组',
  `intro` varchar(4000) DEFAULT NULL COMMENT '个人简介',
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像',
  `mobile` varchar(32) DEFAULT NULL COMMENT '联系电话（敏感）',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号（敏感）',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `photo_urls` text COMMENT '照片集 JSON',
  `document_urls` varchar(4000) DEFAULT NULL COMMENT '文档 JSON',
  `alive` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否在世',
  `province_id` int DEFAULT NULL COMMENT '省',
  `city_id` int DEFAULT NULL COMMENT '市',
  `county_id` int DEFAULT NULL COMMENT '县/区',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `deleted_time` datetime DEFAULT NULL COMMENT '进入回收站时间',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_family_name` (`family_id`, `name`),
  KEY `idx_father` (`father_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家族成员';

DROP TABLE IF EXISTS `tb_member_deed`;
CREATE TABLE `tb_member_deed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '成员编号',
  `title` varchar(64) NOT NULL COMMENT '标题',
  `content` varchar(2000) DEFAULT NULL COMMENT '内容',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `occur_year` varchar(32) DEFAULT NULL COMMENT '发生年份',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员事迹荣誉';

DROP TABLE IF EXISTS `tb_archive_apply`;
CREATE TABLE `tb_archive_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '目标成员',
  `applicant_user_id` bigint NOT NULL COMMENT '申请人用户',
  `content` varchar(2000) NOT NULL COMMENT '补充说明',
  `attachments` varchar(2000) DEFAULT NULL COMMENT '附件 JSON',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `audit_user_id` bigint DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='档案补充申请';

DROP TABLE IF EXISTS `tb_migration_node`;
CREATE TABLE `tb_migration_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `node_time` varchar(64) NOT NULL COMMENT '时间描述',
  `place` varchar(128) NOT NULL COMMENT '地点',
  `event_title` varchar(128) DEFAULT NULL COMMENT '事件',
  `person` varchar(64) DEFAULT NULL COMMENT '人物',
  `description` varchar(1000) DEFAULT NULL COMMENT '详情',
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='迁徙节点';

DROP TABLE IF EXISTS `tb_ancestor_deed`;
CREATE TABLE `tb_ancestor_deed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `member_id` bigint DEFAULT NULL COMMENT '关联成员',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `title` varchar(64) DEFAULT NULL COMMENT '称号',
  `category` varchar(32) DEFAULT NULL COMMENT '分类',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `content` mediumtext COMMENT '事迹正文',
  `cover_url` varchar(512) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='祖先事迹库';

DROP TABLE IF EXISTS `tb_culture_guide`;
CREATE TABLE `tb_culture_guide` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `title` varchar(64) NOT NULL,
  `content` mediumtext,
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0开启 1关闭',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='祭祖文化指南';

DROP TABLE IF EXISTS `tb_feed`;
CREATE TABLE `tb_feed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1公告 2动态 3公示',
  `title` varchar(128) NOT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `images` varchar(2000) DEFAULT NULL COMMENT '图片 JSON',
  `pinned` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否置顶',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0待审 1已发布 2驳回 3下线',
  `author_user_id` bigint DEFAULT NULL,
  `author_name` varchar(32) DEFAULT NULL,
  `like_count` int NOT NULL DEFAULT 0,
  `comment_count` int NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家族动态公告';

DROP TABLE IF EXISTS `tb_feed_comment`;
CREATE TABLE `tb_feed_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `user_name` varchar(32) DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_feed` (`feed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态评论';

DROP TABLE IF EXISTS `tb_feed_like`;
CREATE TABLE `tb_feed_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feed_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_user` (`feed_id`, `user_id`, `deleted`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞';

DROP TABLE IF EXISTS `tb_scholarship_config`;
CREATE TABLE `tb_scholarship_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `year` int NOT NULL COMMENT '学年',
  `window_start` datetime NOT NULL COMMENT '申请开始',
  `window_end` datetime NOT NULL COMMENT '申请结束',
  `policy` varchar(2000) DEFAULT NULL COMMENT '资助政策',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_year` (`family_id`, `year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助窗口配置';

DROP TABLE IF EXISTS `tb_scholarship_application`;
CREATE TABLE `tb_scholarship_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_no` varchar(32) NOT NULL COMMENT '申请编号',
  `family_id` bigint NOT NULL,
  `member_id` bigint NOT NULL COMMENT '申请人成员',
  `user_id` bigint NOT NULL COMMENT '申请人用户',
  `year` int NOT NULL COMMENT '学年',
  `school` varchar(128) NOT NULL,
  `major` varchar(64) NOT NULL,
  `grade` varchar(16) NOT NULL COMMENT '年级',
  `student_no` varchar(32) NOT NULL COMMENT '学号',
  `type` tinyint NOT NULL COMMENT '1助学金 2奖学金 3临时困难补助',
  `suggest_amount` decimal(10,2) DEFAULT NULL COMMENT '建议金额',
  `family_situation` varchar(1000) NOT NULL COMMENT '家庭经济情况',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态，见枚举',
  `reject_reason` varchar(500) DEFAULT NULL,
  `supplement_remark` varchar(500) DEFAULT NULL COMMENT '要求补充说明',
  `materials` varchar(4000) DEFAULT NULL COMMENT '材料 JSON',
  `first_audit_user_id` bigint DEFAULT NULL,
  `first_audit_opinion` varchar(500) DEFAULT NULL,
  `first_audit_time` datetime DEFAULT NULL,
  `final_audit_user_id` bigint DEFAULT NULL,
  `final_audit_opinion` varchar(500) DEFAULT NULL,
  `final_audit_time` datetime DEFAULT NULL,
  `submit_time` datetime DEFAULT NULL,
  `province_id` int DEFAULT NULL COMMENT '申请时省',
  `city_id` int DEFAULT NULL COMMENT '申请时市',
  `county_id` int DEFAULT NULL COMMENT '申请时县',
  `region_name` varchar(128) DEFAULT NULL COMMENT '申请时地区',
  `current_audit_level` varchar(16) DEFAULT NULL COMMENT '当前待审级别',
  `audit_pipeline` varchar(128) DEFAULT NULL COMMENT '审核链路 JSON',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_member_year` (`member_id`, `year`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助申请';

DROP TABLE IF EXISTS `tb_admin_region`;
CREATE TABLE `tb_admin_region` (
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

DROP TABLE IF EXISTS `tb_scholarship_audit_log`;
CREATE TABLE `tb_scholarship_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `audit_level` varchar(16) NOT NULL,
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

DROP TABLE IF EXISTS `tb_scholarship_disbursement`;
CREATE TABLE `tb_scholarship_disbursement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL COMMENT '发放金额',
  `method` tinyint NOT NULL COMMENT '1银行转账 2现金 3其他',
  `disburse_date` datetime NOT NULL COMMENT '发放日期',
  `handler_name` varchar(32) NOT NULL COMMENT '经办人',
  `voucher_urls` varchar(2000) DEFAULT NULL COMMENT '凭证 JSON',
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助发放记录';

DROP TABLE IF EXISTS `tb_activity`;
CREATE TABLE `tb_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `title` varchar(64) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `place` varchar(128) NOT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `process_desc` varchar(2000) DEFAULT NULL COMMENT '活动流程',
  `gather_place` varchar(128) NOT NULL COMMENT '集合地点',
  `notice` varchar(500) DEFAULT NULL COMMENT '注意事项',
  `deadline` datetime NOT NULL COMMENT '报名截止',
  `max_count` int DEFAULT NULL COMMENT '人数上限',
  `contact_name` varchar(32) NOT NULL COMMENT '联系人',
  `contact_mobile` varchar(32) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0草稿 1报名中 2已结束 3已取消',
  `reminded` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已发送提前提醒',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='祭祖活动';

DROP TABLE IF EXISTS `tb_activity_registration`;
CREATE TABLE `tb_activity_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `user_name` varchar(32) DEFAULT NULL,
  `mobile` varchar(32) DEFAULT NULL,
  `people_count` int NOT NULL DEFAULT 1 COMMENT '参与人数',
  `need_bus` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否乘车',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1已报名 2候补 3已取消',
  `reminded` bit(1) NOT NULL DEFAULT b'0',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名';

DROP TABLE IF EXISTS `tb_tomb_site`;
CREATE TABLE `tb_tomb_site` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL COMMENT '地点名称',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `parking` varchar(255) DEFAULT NULL COMMENT '停车场说明',
  `entrance` varchar(255) DEFAULT NULL COMMENT '入口说明',
  `toilet` varchar(255) DEFAULT NULL COMMENT '公厕说明',
  `remark` varchar(500) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='温蒂坟地';

DROP TABLE IF EXISTS `tb_worship_record`;
CREATE TABLE `tb_worship_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `user_name` varchar(32) DEFAULT NULL,
  `content` varchar(500) DEFAULT NULL COMMENT '留言',
  `images` varchar(2000) DEFAULT NULL COMMENT '照片 JSON 最多9张',
  `pinned` bit(1) NOT NULL DEFAULT b'0',
  `online` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否线上祭扫',
  `ancestor_name` varchar(32) DEFAULT NULL COMMENT '线上祭扫先人',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='祭扫记录';

DROP TABLE IF EXISTS `tb_ai_match_message`;
DROP TABLE IF EXISTS `tb_ai_match_conversation`;
CREATE TABLE `tb_ai_match_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话编号',
  `user_id` bigint NOT NULL COMMENT '登录用户',
  `session_id` varchar(64) NOT NULL COMMENT '匿名/流式会话 UUID',
  `title` varchar(128) NOT NULL DEFAULT '新对话' COMMENT '标题',
  `last_score` int DEFAULT NULL COMMENT '最近同族可能性',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session` (`session_id`),
  KEY `idx_user` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI寻宗会话';

CREATE TABLE `tb_ai_match_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息编号',
  `conversation_id` bigint NOT NULL COMMENT '会话编号',
  `role` varchar(16) NOT NULL COMMENT 'user/assistant',
  `content` text NOT NULL COMMENT '内容',
  `score` int DEFAULT NULL COMMENT '同族可能性',
  `contacts` text COMMENT '管理员联系方式 JSON',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI寻宗消息';

-- ===================== 字典 =====================

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
('字辈状态', 'genealogy_generation_status', 0, '在用/已用/备用', '1', NOW(), '1', NOW(), b'0'),
('资助类型', 'genealogy_scholarship_type', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('资助申请状态', 'genealogy_scholarship_status', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('发放方式', 'genealogy_disburse_method', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('祭祖活动状态', 'genealogy_activity_status', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('动态类型', 'genealogy_feed_type', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('动态状态', 'genealogy_feed_status', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('年级', 'genealogy_grade', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('事迹分类', 'genealogy_deed_category', 0, NULL, '1', NOW(), '1', NOW(), b'0'),
('档案审核状态', 'genealogy_archive_status', 0, NULL, '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(1, '在用', '1', 'genealogy_generation_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '已用', '2', 'genealogy_generation_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '备用', '3', 'genealogy_generation_status', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '助学金', '1', 'genealogy_scholarship_type', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '奖学金', '2', 'genealogy_scholarship_type', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '临时困难补助', '3', 'genealogy_scholarship_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(0, '草稿', '0', 'genealogy_scholarship_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '待初审', '1', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '待补充材料', '2', 'genealogy_scholarship_status', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '初审通过', '3', 'genealogy_scholarship_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(4, '待发放', '4', 'genealogy_scholarship_status', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(5, '已发放', '5', 'genealogy_scholarship_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(6, '已驳回', '6', 'genealogy_scholarship_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(7, '已撤回', '7', 'genealogy_scholarship_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(10, '待县审', '10', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(11, '待市审', '11', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(12, '待省审', '12', 'genealogy_scholarship_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(13, '待家族审', '13', 'genealogy_scholarship_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '银行转账', '1', 'genealogy_disburse_method', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '现金', '2', 'genealogy_disburse_method', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '其他', '3', 'genealogy_disburse_method', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(0, '草稿', '0', 'genealogy_activity_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '报名中', '1', 'genealogy_activity_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '已结束', '2', 'genealogy_activity_status', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '已取消', '3', 'genealogy_activity_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '公告', '1', 'genealogy_feed_type', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '动态', '2', 'genealogy_feed_type', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '公示', '3', 'genealogy_feed_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(0, '待审', '0', 'genealogy_feed_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '已发布', '1', 'genealogy_feed_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '已驳回', '2', 'genealogy_feed_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '已下线', '3', 'genealogy_feed_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '大一', '大一', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '大二', '大二', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '大三', '大三', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(4, '大四', '大四', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(5, '研一', '研一', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(6, '研二', '研二', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(7, '研三', '研三', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(8, '博士', '博士', 'genealogy_grade', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '文官', '文官', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '武将', '武将', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(3, '学者', '学者', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(4, '义士', '义士', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(5, '乡贤', '乡贤', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(6, '医者', '医者', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(7, '商贾', '商贾', 'genealogy_deed_category', 0, '', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(0, '待审', '0', 'genealogy_archive_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1, '通过', '1', 'genealogy_archive_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(2, '驳回', '2', 'genealogy_archive_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0');

-- ===================== 菜单 =====================
-- type: 1目录 2菜单 3按钮；status 0开启

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6800, '族谱系统', '', 1, 10, 0, '/genealogy', 'ep:family', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6801, '工作台', 'genealogy:dashboard:query', 2, 1, 6800, 'home', 'ep:data-board', 'genealogy/home/index', 'GenealogyHome', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6802, '族谱管理', '', 1, 2, 6800, 'family', 'ep:user', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6803, '成员管理', 'genealogy:member:query', 2, 1, 6802, 'member', 'ep:avatar', 'genealogy/member/index', 'GenealogyMember', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6804, '谱系编辑', 'genealogy:member:update', 2, 2, 6802, 'pedigree', 'ep:share', 'genealogy/pedigree/index', 'GenealogyPedigree', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6805, '档案审核', 'genealogy:archive:query', 2, 3, 6802, 'archive', 'ep:document', 'genealogy/archive/index', 'GenealogyArchive', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6806, '字辈派语', 'genealogy:content:query', 2, 4, 6802, 'generation', 'ep:collection-tag', 'genealogy/generation/index', 'GenealogyGeneration', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6807, '地区管理员', 'genealogy:admin-region:query', 2, 5, 6802, 'region-admin', 'ep:location', 'genealogy/region-admin/index', 'GenealogyRegionAdmin', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6810, '学海无涯', '', 1, 3, 6800, 'scholarship', 'ep:reading', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6811, '申请审核', 'genealogy:scholarship:query', 2, 1, 6810, 'audit', 'ep:checked', 'genealogy/scholarship/audit/index', 'GenealogyScholarshipAudit', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6812, '发放登记', 'genealogy:disburse:query', 2, 2, 6810, 'disburse', 'ep:money', 'genealogy/scholarship/disburse/index', 'GenealogyDisburse', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6813, '公示与统计', 'genealogy:scholarship:stats', 2, 3, 6810, 'stats', 'ep:data-analysis', 'genealogy/scholarship/stats/index', 'GenealogyScholarshipStats', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6820, '祭祖活动', '', 1, 4, 6800, 'worship', 'ep:sunset', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6821, '活动管理', 'genealogy:activity:query', 2, 1, 6820, 'activity', 'ep:calendar', 'genealogy/activity/index', 'GenealogyActivity', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6822, '内容管理', 'genealogy:content:query', 2, 2, 6820, 'content', 'ep:notebook', 'genealogy/content/index', 'GenealogyContent', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6830, '运营与系统', '', 1, 5, 6800, 'ops', 'ep:setting', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6831, '动态公告', 'genealogy:feed:query', 2, 1, 6830, 'feed', 'ep:bell', 'genealogy/feed/index', 'GenealogyFeed', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6832, '系统设置', 'genealogy:config:query', 2, 2, 6830, 'config', 'ep:tools', 'genealogy/config/index', 'GenealogyConfig', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(6901, '成员新增', 'genealogy:member:create', 3, 1, 6803, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6902, '成员更新', 'genealogy:member:update', 3, 2, 6803, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6903, '成员删除', 'genealogy:member:delete', 3, 3, 6803, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6904, '成员导入', 'genealogy:member:import', 3, 4, 6803, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6905, '成员导出', 'genealogy:member:export', 3, 5, 6803, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6906, '字辈维护', 'genealogy:content:update', 3, 1, 6806, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6908, '地区绑定', 'genealogy:admin-region:update', 3, 1, 6807, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6911, '档案审核', 'genealogy:archive:audit', 3, 1, 6805, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6921, '资助初审', 'genealogy:scholarship:first-audit', 3, 1, 6811, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6922, '资助终审', 'genealogy:scholarship:final-audit', 3, 2, 6811, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6931, '发放登记', 'genealogy:disburse:create', 3, 1, 6812, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6932, '发放导出', 'genealogy:disburse:export', 3, 2, 6812, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6941, '活动创建', 'genealogy:activity:create', 3, 1, 6821, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6942, '活动更新', 'genealogy:activity:update', 3, 2, 6821, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6943, '活动删除', 'genealogy:activity:delete', 3, 3, 6821, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6951, '内容维护', 'genealogy:content:update', 3, 1, 6822, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6961, '动态发布', 'genealogy:feed:create', 3, 1, 6831, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6962, '动态审核', 'genealogy:feed:audit', 3, 2, 6831, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(6971, '系统设置更新', 'genealogy:config:update', 3, 1, 6832, '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 角色：族长 / 管理员 / 普通族人 / 省市区管理员
INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(201, '族长', 'genealogy_patriarch', 1, 1, '', 0, 2, '家族最高管理者', '1', NOW(), '1', NOW(), b'0', 1),
(202, '管理员', 'genealogy_admin', 2, 1, '', 0, 2, '族长指定的协助管理者', '1', NOW(), '1', NOW(), b'0', 1),
(203, '普通族人', 'genealogy_member', 3, 1, '', 0, 2, '已认证家族成员', '1', NOW(), '1', NOW(), b'0', 1),
(204, '省管理员', 'genealogy_province_admin', 4, 1, '', 0, 2, '省级管理员，可进入管理端', '1', NOW(), '1', NOW(), b'0', 1),
(205, '市管理员', 'genealogy_city_admin', 5, 1, '', 0, 2, '市级管理员，可进入管理端', '1', NOW(), '1', NOW(), b'0', 1),
(206, '县管理员', 'genealogy_county_admin', 6, 1, '', 0, 2, '县级管理员，可进入管理端', '1', NOW(), '1', NOW(), b'0', 1);

-- 族长：全部族谱菜单；管理员及省/市/县：除系统设置外
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 201, `id`, '1', NOW(), '1', NOW(), b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 6800 AND 6971;
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 202, `id`, '1', NOW(), '1', NOW(), b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 6800 AND 6971 AND `id` NOT IN (6832, 6971);
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 204, `id`, '1', NOW(), '1', NOW(), b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 6800 AND 6971 AND `id` NOT IN (6832, 6971);
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 205, `id`, '1', NOW(), '1', NOW(), b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 6800 AND 6971 AND `id` NOT IN (6832, 6971);
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT 206, `id`, '1', NOW(), '1', NOW(), b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 6800 AND 6971 AND `id` NOT IN (6832, 6971);

-- ===================== 种子数据 =====================

INSERT INTO `tb_family` (`id`, `name`, `surname`, `ancestor_name`, `ancestor_id`, `region`, `intro`, `origin_content`, `book_title`, `book_revision`, `book_preface`, `patriarch_user_id`, `tenant_id`, `creator`)
VALUES (1, '陈氏族谱', '陈', '陈公远', 1, '贵州 · 贵阳',
'陈氏一支，明末自江西吉安迁入湖广，清康熙年间入黔，定居贵阳乌当，迄今三百余年，枝叶繁茂，人丁兴旺。始祖陈公远公，勤俭起家，以耕读传家，后世子孙遍布黔中及省内外，代有才俊。',
'<p>陈氏出自妫姓，舜帝之后。本支始祖陈公远，清乾隆年间自遵义迁居贵阳乌当，披荆斩棘，拓荒立业。郡望颍川，堂号德星。</p>',
'颍川陈氏族谱', '二〇二六年春重修',
'<p>盖闻木有本而枝荣，水有源而流长。陈氏出自妫姓，舜帝之后，郡望颍川。本支始祖公远公，清乾隆年间自遵义迁居贵阳乌当，披荆斩棘，拓荒立业，耕读传家。</p><p>今据世系档案，按辈排纂，成此谱书，俾子孙开卷可知所出、所承、所传。是为序。</p>',
1, 1, '1');

INSERT INTO `tb_generation` (`id`, `family_id`, `generation_no`, `word`, `status`, `remark`, `sort`, `tenant_id`, `creator`) VALUES
(1, 1, 1, '永', 2, '始祖以下第一世', 1, 1, '1'),
(2, 1, 2, '昌', 2, NULL, 2, 1, '1'),
(3, 1, 3, '世', 2, NULL, 3, 1, '1'),
(4, 1, 4, '德', 2, NULL, 4, 1, '1'),
(10, 1, 4, '万', 3, '同一世并行字辈示例', 41, 1, '1'),
(5, 1, 5, '文', 1, '当前取名使用', 5, 1, '1'),
(6, 1, 6, '明', 1, '当前取名使用', 6, 1, '1'),
(7, 1, 7, '绍', 1, '当前取名使用', 7, 1, '1'),
(8, 1, 8, '启', 3, '备选字辈', 8, 1, '1'),
(9, 1, 9, '家', 3, '备选字辈', 9, 1, '1');

INSERT INTO `tb_member` (`id`, `family_id`, `user_id`, `name`, `gender`, `generation_id`, `generation_no`, `birth_date`, `death_date`, `father_id`, `spouse_ids`, `intro`, `tags`, `alive`, `tenant_id`, `creator`) VALUES
(1, 1, NULL, '陈公远', 1, 1, 1, '1760-01-01', '1832-01-01', NULL, '[2]', '陈氏入黔始祖。清乾隆年间自遵义迁居贵阳乌当，勤俭起家，拓荒置业，为家族奠基之人。', '始祖', b'0', 1, '1'),
(2, 1, NULL, '陈张氏', 2, 1, 1, '1763-01-01', '1830-01-01', NULL, '[1]', '始祖妣张氏，贤淑持家，相夫教子。', NULL, b'0', 1, '1'),
(3, 1, NULL, '陈永昌', 1, 2, 2, '1785-01-01', '1852-01-01', 1, '[5]', '二世祖，承父业广置田产，乐善好施，设义学教化乡里。', '兴学', b'0', 1, '1'),
(4, 1, NULL, '陈永福', 1, 2, 2, '1790-01-01', '1855-01-01', 1, NULL, '二世祖，务农为本，安分守己。', NULL, b'0', 1, '1'),
(5, 1, NULL, '陈李氏', 2, 2, 2, '1787-01-01', '1850-01-01', NULL, '[3]', NULL, NULL, b'0', 1, '1'),
(6, 1, NULL, '陈昌明', 1, 3, 3, '1810-01-01', '1878-01-01', 3, '[8]', '三世祖，精研岐黄，行医乡里数十年，救人无数，人称“陈善医”。', '医者', b'0', 1, '1'),
(7, 1, NULL, '陈昌盛', 1, 3, 3, '1815-01-01', '1861-01-01', 3, NULL, '三世祖，早逝。', NULL, b'0', 1, '1'),
(8, 1, NULL, '陈王氏', 2, 3, 3, '1812-01-01', '1875-01-01', NULL, '[6]', NULL, NULL, b'0', 1, '1'),
(9, 1, NULL, '陈世文', 1, 4, 4, '1838-01-01', '1902-01-01', 6, '[11]', '四世祖，设私塾授徒，桃李满门，晚年主持修纂族谱。', '修谱', b'0', 1, '1'),
(10, 1, NULL, '陈世武', 1, 4, 4, '1842-01-01', '1901-01-01', 6, NULL, '四世祖，经营茶行。', NULL, b'0', 1, '1'),
(11, 1, NULL, '陈刘氏', 2, 4, 4, '1840-01-01', '1898-01-01', NULL, '[9]', NULL, NULL, b'0', 1, '1'),
(12, 1, NULL, '陈德厚', 1, 5, 5, '1866-01-01', NULL, 9, '[14]', '五世祖，热心公益，出资兴办族学。现任家族族长。', '族长,商贾', b'1', 1, '1'),
(13, 1, NULL, '陈德馨', 2, 5, 5, '1870-01-01', '1950-01-01', 9, NULL, '五世祖姑，持家育子，德高望重。', NULL, b'0', 1, '1'),
(14, 1, NULL, '陈周氏', 2, 5, 5, '1868-01-01', '1945-01-01', NULL, '[12]', NULL, NULL, b'0', 1, '1'),
(15, 1, 1, '陈文轩', 1, 6, 6, '1994-06-18', NULL, 12, NULL, '现任家族族谱管理员，热衷整理家族史料。', '管理员', b'1', 1, '1'),
(16, 1, NULL, '陈文静', 2, 6, 6, '1998-03-12', NULL, 12, NULL, '贵州大学在读，积极参与家族活动。', '在读', b'1', 1, '1');

INSERT INTO `tb_migration_node` (`family_id`, `node_time`, `place`, `event_title`, `person`, `description`, `longitude`, `latitude`, `sort`, `tenant_id`, `creator`) VALUES
(1, '明末崇祯', '江西吉安', '避乱南迁，溯赣江入湖广', '陈公远先世', '始祖公远公先世居江西吉安府，明末战乱，举家南迁。', 114.986373, 27.111698, 1, 1, '1'),
(1, '清康熙二十九年', '湖广→贵州遵义', '随“湖广填四川”西迁入黔', '陈公远', '公远公随移民潮自湖广辗转进入贵州，初居遵义府。', 106.934326, 27.706626, 2, 1, '1'),
(1, '清乾隆四十六年', '遵义→贵阳乌当', '定居温蒂，拓荒置业', '陈公远', '公远公迁居贵阳乌当温蒂，置田建宅，开枝散叶。', 106.753094, 26.630928, 3, 1, '1'),
(1, '当代', '贵阳→省内外', '族裔外迁，枝繁叶茂', '后世子孙', '后世子孙求学、经商、为官，遍布黔中及省内外。', 106.713478, 26.578343, 4, 1, '1');

INSERT INTO `tb_ancestor_deed` (`family_id`, `member_id`, `name`, `title`, `category`, `source`, `content`, `tenant_id`, `creator`) VALUES
(1, 1, '陈公远', '入黔始祖', '义士', '族谱记载', '清乾隆年间自遵义迁居贵阳乌当，披荆斩棘，拓荒立业，为陈氏在黔第一人。俭以养德，耕读传家，立下家训：“勤俭为本，忠厚传家”。', 1, '1'),
(1, 3, '陈永昌', '兴学义士', '乡贤', '族谱记载', '二世祖承父业广置田产，乐善好施。出资设立义学，延师教读，使族中子弟皆得读书明理。', 1, '1'),
(1, 6, '陈昌明', '一方名医', '医者', '族谱记载·乡志', '三世祖精研岐黄之术，行医乡里五十余年，救人无数，贫者施药不受金，乡人称颂“陈善医”。', 1, '1'),
(1, 12, '陈德厚', '商会理事·族长', '商贾', '家族口述', '五世祖热心公益，出资兴办族学，培育后进，泽被乡梓。', 1, '1');

INSERT INTO `tb_feed` (`family_id`, `type`, `title`, `content`, `pinned`, `status`, `author_user_id`, `author_name`, `tenant_id`, `creator`) VALUES
(1, 1, '2026年度助学资助名单公示', '经初审与终审，本年度共有 3 位族人获得家族助学金资助，现予公示，公示期 7 天。', b'1', 1, 1, '族长', 1, '1'),
(1, 2, '清明祭祖活动圆满举行', '感谢各位族人踊跃参与，温蒂坟地祭扫顺利完成，现场 52 位族人齐聚，缅怀先祖。', b'0', 1, 1, '陈文轩', 1, '1'),
(1, 1, '2026年度资助申请通道开放', '2026年度在校大学生资助申请现已开放，窗口期 7月1日-8月31日，符合条件者请及时提交。', b'0', 1, 1, '族长', 1, '1'),
(1, 2, '族谱数字化录入进展', '经过数月整理，本族自始祖至今共录入 200 余位族人信息，谱系树已初具规模。', b'0', 1, 1, '陈文轩', 1, '1');

INSERT INTO `tb_scholarship_config` (`family_id`, `year`, `window_start`, `window_end`, `policy`, `tenant_id`, `creator`)
VALUES (1, 2026, '2026-07-01 00:00:00', '2026-08-31 23:59:59',
'本家族设立“学海无涯”助学基金，资助本族全日制在校大学生（本科/专科/研究生）。资助类型：助学金（家庭经济困难）、奖学金（学业优异）、临时困难补助（突发变故）。申请人须为族谱认证成员，按要求提交在校证明、成绩单等材料，经初审、终审通过后发放。',
1, '1');

INSERT INTO `tb_scholarship_application` (`apply_no`, `family_id`, `member_id`, `user_id`, `year`, `school`, `major`, `grade`, `student_no`, `type`, `suggest_amount`, `family_situation`, `status`, `materials`, `submit_time`, `tenant_id`, `creator`) VALUES
('JD2026-001', 1, 16, 1, 2026, '贵州大学', '汉语言文学', '大二', '202401001', 1, 4000, '家庭经济一般，父母务农。', 1, '[{"type":"ENROLL","name":"在校证明.pdf"},{"type":"TRANSCRIPT","name":"成绩单.pdf"}]', '2026-07-15 10:00:00', 1, '1'),
('JD2026-003', 1, 15, 1, 2026, '西南交通大学', '土木工程', '研一', '202501088', 2, 6000, '学业优异。', 5, '[{"type":"ENROLL","name":"在校证明.pdf"},{"type":"TRANSCRIPT","name":"成绩单.pdf"}]', '2026-06-20 10:00:00', 1, '1');

INSERT INTO `tb_scholarship_disbursement` (`application_id`, `amount`, `method`, `disburse_date`, `handler_name`, `tenant_id`, `creator`)
VALUES (2, 6000, 1, '2026-08-01 00:00:00', '陈德厚', 1, '1');

INSERT INTO `tb_tomb_site` (`family_id`, `name`, `address`, `longitude`, `latitude`, `parking`, `entrance`, `toilet`, `tenant_id`, `creator`)
VALUES (1, '温蒂坟地', '贵州省贵阳市乌当区温蒂村后山（始祖陈公远墓）', 106.812000, 26.652000, '村口空地可停约 20 辆小型车辆', '后山石阶入口，沿小路上行约 300 米', '村委会旁公共卫生间', 1, '1');

INSERT INTO `tb_activity` (`id`, `family_id`, `title`, `start_time`, `end_time`, `place`, `longitude`, `latitude`, `process_desc`, `gather_place`, `notice`, `deadline`, `max_count`, `contact_name`, `status`, `tenant_id`, `creator`) VALUES
(1, 1, '2026年清明祭祖', '2026-04-05 09:00:00', '2026-04-05 16:00:00', '温蒂坟地（贵阳乌当区）', 106.812000, 26.652000, '上午祭扫，中午族祠聚餐。', '族祠广场', '请携带祭品与鲜花。', '2026-04-02 23:59:59', 60, '陈德厚', 2, 1, '1'),
(2, 1, '2027年清明祭祖', '2027-04-05 09:00:00', '2027-04-05 16:00:00', '温蒂坟地（贵阳乌当区）', 106.812000, 26.652000, '集体祭扫，提供大巴接送。', '族祠广场', '欢迎族人踊跃报名。', '2027-04-02 23:59:59', 80, '陈德厚', 1, 1, '1');

INSERT INTO `tb_activity_registration` (`activity_id`, `user_id`, `member_id`, `user_name`, `mobile`, `people_count`, `need_bus`, `status`, `tenant_id`, `creator`) VALUES
(2, 1, 15, '陈文轩', '13800008866', 2, b'1', 1, 1, '1');

INSERT INTO `tb_worship_record` (`activity_id`, `user_id`, `user_name`, `content`, `tenant_id`, `creator`) VALUES
(1, 1, '陈文轩', '2026年清明，温蒂坟地祭扫，52位族人齐聚，敬献花篮，追思始祖。', 1, '1');

INSERT INTO `tb_culture_guide` (`family_id`, `title`, `content`, `sort`, `status`, `tenant_id`, `creator`) VALUES
(1, '清明祭祖礼仪', '<p>1. 衣着整洁素雅；2. 按辈分依次上香鞠躬；3. 勿喧哗踩踏坟茔；4. 祭品以鲜花时果为宜。</p>', 1, 0, 1, '1');

-- 关闭租户功能：隐藏系统管理中的租户菜单
UPDATE `system_menu` SET `status` = 1, `visible` = b'0'
WHERE `id` IN (1138, 1139, 1140, 1141, 1142, 1143, 1224, 1225, 1226, 1227, 1228, 1229, 5010);

SET FOREIGN_KEY_CHECKS = 1;
