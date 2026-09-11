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
    /** 全国统一字派（数据字典 genealogy_generation_source） */
    private String nationalSource;
    /** 高安椒坊字派 */
    private String jiaofangSource;
    /** 所属房：1长房 2二房 3三房 4三房织金 5四五房 */
    private String house;
    /** 1在用 2已用 3备用 */
    private Integer status;
    private String remark;
    private Integer sort;
}
