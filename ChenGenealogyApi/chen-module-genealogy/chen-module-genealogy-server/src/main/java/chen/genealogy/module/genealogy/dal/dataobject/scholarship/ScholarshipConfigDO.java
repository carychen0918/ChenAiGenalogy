package chen.genealogy.module.genealogy.dal.dataobject.scholarship;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("tb_scholarship_config")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipConfigDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private Integer year;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String policy;
}
