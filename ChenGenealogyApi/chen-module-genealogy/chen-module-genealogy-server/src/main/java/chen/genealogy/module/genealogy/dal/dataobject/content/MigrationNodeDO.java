package chen.genealogy.module.genealogy.dal.dataobject.content;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

@TableName("tb_migration_node")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationNodeDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private String nodeTime;
    private String place;
    private String eventTitle;
    private String person;
    private String description;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer sort;
}
