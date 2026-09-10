-- 谱书：家族公共信息（前言/谱名/重修记）
-- 已有库执行本脚本即可，无需重建

ALTER TABLE `tb_family`
  ADD COLUMN `book_title` varchar(64) DEFAULT NULL COMMENT '谱书名称' AFTER `origin_content`,
  ADD COLUMN `book_revision` varchar(32) DEFAULT NULL COMMENT '重修记' AFTER `book_title`,
  ADD COLUMN `book_preface` mediumtext COMMENT '谱书前言/谱序富文本' AFTER `book_revision`;

UPDATE `tb_family`
SET `book_title` = IFNULL(`book_title`, CONCAT(`name`, '谱书')),
    `book_revision` = IFNULL(`book_revision`, '二〇二六年春重修'),
    `book_preface` = IFNULL(`book_preface`,
      '<p>盖闻木有本而枝荣，水有源而流长。今据世系档案，按辈排纂，成此谱书，俾子孙开卷可知所出、所承、所传。是为序。</p>')
WHERE `id` = 1;
