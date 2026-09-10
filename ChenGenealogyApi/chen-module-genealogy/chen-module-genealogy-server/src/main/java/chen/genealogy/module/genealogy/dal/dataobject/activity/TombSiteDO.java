package chen.genealogy.module.genealogy.dal.dataobject.activity;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

@TableName("tb_tomb_site")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TombSiteDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String parking;
    private String entrance;
    private String toilet;
    private String remark;
}
