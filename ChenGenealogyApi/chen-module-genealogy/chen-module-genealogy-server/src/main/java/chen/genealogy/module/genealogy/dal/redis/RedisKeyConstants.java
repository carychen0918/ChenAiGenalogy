package chen.genealogy.module.genealogy.dal.redis;

/**
 * 族谱模块 Redis Key
 */
public interface RedisKeyConstants {

    /**
     * 全量世系成员（已拼好字辈、父母配偶名，未脱敏）
     * <p>
     * KEY：genealogy_member_pedigree:{familyId}
     * TTL：6 小时，成员或字辈变更时主动失效
     */
    String MEMBER_PEDIGREE = "genealogy_member_pedigree#6h";
}
