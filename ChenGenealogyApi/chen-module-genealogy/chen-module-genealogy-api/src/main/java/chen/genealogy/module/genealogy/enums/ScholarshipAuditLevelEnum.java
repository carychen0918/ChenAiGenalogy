package chen.genealogy.module.genealogy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScholarshipAuditLevelEnum {

    COUNTY("county", "县审", 10),
    CITY("city", "市审", 11),
    PROVINCE("province", "省审", 12),
    FAMILY("family", "家族审", 13);

    private final String code;
    private final String name;
    private final Integer pendingStatus;

    public static ScholarshipAuditLevelEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ScholarshipAuditLevelEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
