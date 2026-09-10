-- 成员生平简介加长，照片集改为 TEXT，便于管理员维护档案
ALTER TABLE `tb_member`
  MODIFY COLUMN `intro` varchar(4000) DEFAULT NULL COMMENT '个人简介',
  MODIFY COLUMN `photo_urls` text COMMENT '照片集 JSON';
