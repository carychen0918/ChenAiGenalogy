package chen.genealogy.module.genealogy.dal.dataobject.showcase;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

@TableName(value = "tb_showcase_config", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseConfigDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private Long featuredMemberId;
    private Long featuredDeedId;
    private Long featuredAncestorDeedId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> wallApplicationIds;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> calendarMemberIds;
}
