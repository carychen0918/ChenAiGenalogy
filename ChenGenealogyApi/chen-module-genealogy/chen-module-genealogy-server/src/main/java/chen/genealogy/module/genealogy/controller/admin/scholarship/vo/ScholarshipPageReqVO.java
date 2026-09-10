package chen.genealogy.module.genealogy.controller.admin.scholarship.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScholarshipPageReqVO extends PageParam {
    private Integer year;
    private Integer status;
    private Integer type;
    private Long userId;
    private Long memberId;
    private String school;
    private String applyNo;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String currentAuditLevel;
}
