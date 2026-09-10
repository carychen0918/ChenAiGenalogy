package chen.genealogy.module.genealogy.dal.dataobject.scholarship;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "tb_scholarship_application", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipApplicationDO extends TenantBaseDO {

    @TableId
    private Long id;
    private String applyNo;
    private Long familyId;
    private Long memberId;
    private Long userId;
    private Integer year;
    private String school;
    private String major;
    private String grade;
    private String studentNo;
    /** 1助学金 2奖学金 3临时困难补助 */
    private Integer type;
    private BigDecimal suggestAmount;
    private String familySituation;
    private Integer status;
    private String rejectReason;
    private String supplementRemark;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ApplicationMaterial> materials;
    private Long firstAuditUserId;
    private String firstAuditOpinion;
    private LocalDateTime firstAuditTime;
    private Long finalAuditUserId;
    private String finalAuditOpinion;
    private LocalDateTime finalAuditTime;
    private LocalDateTime submitTime;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
    private String regionName;
    private String currentAuditLevel;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> auditPipeline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationMaterial {
        /** ENROLL / TRANSCRIPT / ADMISSION / HARDSHIP */
        private String type;
        private String name;
        private String url;
    }
}
