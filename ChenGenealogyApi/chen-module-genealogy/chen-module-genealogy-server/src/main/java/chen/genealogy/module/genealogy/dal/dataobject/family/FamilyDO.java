package chen.genealogy.module.genealogy.dal.dataobject.family;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_family")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDO extends TenantBaseDO {

    @TableId
    private Long id;
    private String name;
    private String surname;
    private String ancestorName;
    private Long ancestorId;
    private String region;
    private String intro;
    private String originContent;
    private Long patriarchUserId;
    private String logo;
}
