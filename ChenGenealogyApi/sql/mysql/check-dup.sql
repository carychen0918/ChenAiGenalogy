SELECT id, name, user_id FROM tb_member WHERE deleted = 0 AND user_id IS NOT NULL;
SELECT user_id, COUNT(*) AS c FROM tb_member WHERE deleted = 0 AND user_id IS NOT NULL GROUP BY user_id HAVING c > 1;
SELECT id, family_id, name FROM tb_tomb_site WHERE deleted = 0;
SELECT id, family_id, year FROM tb_scholarship_config WHERE deleted = 0;
SELECT id, family_id FROM tb_family WHERE deleted = 0;
SELECT generation_no, COUNT(*) AS c FROM tb_generation WHERE deleted = 0 GROUP BY generation_no HAVING c > 1;
