package chen.genealogy.module.genealogy.dal.dataobject.aimatch;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import chen.genealogy.module.genealogy.controller.admin.aimatch.vo.AiMatchContactVO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

@TableName(value = "tb_ai_match_message", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchMessageDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private Integer score;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AiMatchContactVO> contacts;
}
