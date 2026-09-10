package chen.genealogy.module.genealogy.dal.dataobject.content;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_culture_guide")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CultureGuideDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private String title;
    private String content;
    private Integer sort;
    private Integer status;
}
