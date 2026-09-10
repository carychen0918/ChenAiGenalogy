package chen.genealogy.module.genealogy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资助申请状态
 */
@Getter
@AllArgsConstructor
public enum ScholarshipStatusEnum {

    DRAFT(0, "草稿"),
    PENDING_FIRST(1, "待初审"),
    NEED_MATERIAL(2, "待补充材料"),
    FIRST_PASSED(3, "初审通过"),
    PENDING_DISBURSE(4, "待发放"),
    DISBURSED(5, "已发放"),
    REJECTED(6, "已驳回"),
    WITHDRAWN(7, "已撤回"),
    PENDING_COUNTY(10, "待县审"),
    PENDING_CITY(11, "待市审"),
    PENDING_PROVINCE(12, "待省审"),
    PENDING_FAMILY(13, "待家族审");

    private final Integer status;
    private final String name;

    public static ScholarshipStatusEnum of(Integer status) {
        if (status == null) {
            return null;
        }
        for (ScholarshipStatusEnum value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        return null;
    }
}
