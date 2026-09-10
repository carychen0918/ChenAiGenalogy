package chen.genealogy.module.genealogy.dal.dataobject.generation;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_generation")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private Integer generationNo;
    private String word;
    /** 1在用 2已用 3备用 */
    private Integer status;
    private String remark;
    private Integer sort;
}
