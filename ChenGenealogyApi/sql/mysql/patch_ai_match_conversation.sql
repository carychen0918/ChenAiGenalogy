CREATE TABLE IF NOT EXISTS `tb_ai_match_conversation` (
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

CREATE TABLE IF NOT EXISTS `tb_ai_match_message` (
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
