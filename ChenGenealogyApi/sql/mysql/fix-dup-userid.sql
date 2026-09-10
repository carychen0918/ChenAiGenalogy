-- 陈德厚(id=12) 不应绑定管理员账号；仅保留陈文轩(id=15) 的 user_id=1
UPDATE tb_member SET user_id = NULL WHERE id = 12 AND user_id = 1;

SELECT id, name, user_id FROM tb_member WHERE deleted = 0 AND user_id IS NOT NULL;
SELECT user_id, COUNT(*) AS c FROM tb_member WHERE deleted = 0 AND user_id IS NOT NULL GROUP BY user_id HAVING c > 1;
