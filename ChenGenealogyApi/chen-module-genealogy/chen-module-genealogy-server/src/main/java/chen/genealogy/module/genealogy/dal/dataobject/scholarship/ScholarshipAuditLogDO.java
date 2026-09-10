package chen.genealogy.module.genealogy.dal.dataobject.scholarship;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_scholarship_audit_log")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipAuditLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long applicationId;
    private String auditLevel;
    /** 1通过 2驳回 3补材料 */
    private Integer result;
    private String opinion;
    private Long auditorUserId;
    private String auditorName;
}
