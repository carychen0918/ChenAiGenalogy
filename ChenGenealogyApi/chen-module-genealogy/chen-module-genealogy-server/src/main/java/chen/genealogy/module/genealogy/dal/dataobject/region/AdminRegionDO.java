package chen.genealogy.module.genealogy.dal.dataobject.region;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_admin_region")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegionDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userId;
    /** county / city / province */
    private String auditLevel;
    private Integer areaId;
    private Integer provinceId;
    private Integer cityId;
    private Integer countyId;
}
