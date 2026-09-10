package chen.genealogy.module.genealogy.dal.dataobject.aimatch;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_ai_match_conversation")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchConversationDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String sessionId;
    private String title;
    private Integer lastScore;
}
