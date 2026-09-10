package chen.genealogy.module.system.enums.permission;

import chen.genealogy.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色标识枚举
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    SUPER_ADMIN("super_admin", "超级管理员"),
    TENANT_ADMIN("tenant_admin", "租户管理员"),
    CRM_ADMIN("crm_admin", "CRM 管理员"), // CRM 系统专用
    PATRIARCH("genealogy_patriarch", "族长"),
    GENEALOGY_ADMIN("genealogy_admin", "家族管理员"),
    PROVINCE_ADMIN("genealogy_province_admin", "省管理员"),
    CITY_ADMIN("genealogy_city_admin", "市管理员"),
    COUNTY_ADMIN("genealogy_county_admin", "县管理员");

    /**
     * 角色编码
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    public static boolean isSuperAdmin(String code) {
        return ObjectUtils.equalsAny(code, SUPER_ADMIN.getCode());
    }

    /**
     * 是否允许进入管理端。普通族人、common 等角色返回 false。
     */
    public static boolean isAdminConsole(String code) {
        return ObjectUtils.equalsAny(code,
                SUPER_ADMIN.getCode(),
                TENANT_ADMIN.getCode(),
                PATRIARCH.getCode(),
                GENEALOGY_ADMIN.getCode(),
                PROVINCE_ADMIN.getCode(),
                CITY_ADMIN.getCode(),
                COUNTY_ADMIN.getCode());
    }

}
