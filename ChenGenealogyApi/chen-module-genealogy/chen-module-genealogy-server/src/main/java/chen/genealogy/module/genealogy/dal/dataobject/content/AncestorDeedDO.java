package chen.genealogy.module.genealogy.dal.dataobject.content;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_ancestor_deed")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AncestorDeedDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private Long memberId;
    private String name;
    private String title;
    private String category;
    private String source;
    private String content;
    private String coverUrl;
}
