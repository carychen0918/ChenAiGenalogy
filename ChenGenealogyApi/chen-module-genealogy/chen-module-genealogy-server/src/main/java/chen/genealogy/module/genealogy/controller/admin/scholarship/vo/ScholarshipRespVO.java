package chen.genealogy.module.genealogy.controller.admin.scholarship.vo;

import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScholarshipRespVO {
    private Long id;
    private String applyNo;
    private Long memberId;
    private String memberName;
    private Integer gender;
    private String memberAvatar;
    private String memberMobile;
    private Integer generationNo;
    private String generationWord;
    private Long userId;
    private Integer year;
    private String school;
    private String major;
    private String grade;
    private String studentNo;
    private Integer type;
    private BigDecimal suggestAmount;
    private String familySituation;
    private Integer status;
    private String rejectReason;
    private String supplementRemark;
    private List<ScholarshipApplicationDO.ApplicationMaterial> materials;
    private String firstAuditOpinion;
    private LocalDateTime firstAuditTime;
    private String finalAuditOpinion;
    private LocalDateTime finalAuditTime;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String regionName;
    private String currentAuditLevel;
    private List<String> auditPipeline;
    private boolean canAudit;
    private List<AuditStepVO> auditSteps;
    private List<AuditLogVO> auditLogs;

    @Data
    public static class AuditStepVO {
        private String level;
        private String name;
        /** pending / passed / rejected / skipped / waiting / supplement */
        private String state;
        private String remark;
        private String auditorName;
        private LocalDateTime auditTime;
        private String opinion;
    }

    @Data
    public static class AuditLogVO {
        private String auditLevel;
        private Integer result;
        private String opinion;
        private String auditorName;
        private LocalDateTime createTime;
    }
}
