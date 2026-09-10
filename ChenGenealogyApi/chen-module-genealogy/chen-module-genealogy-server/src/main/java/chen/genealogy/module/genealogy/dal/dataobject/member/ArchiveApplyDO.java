package chen.genealogy.module.genealogy.dal.dataobject.member;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "tb_archive_apply", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveApplyDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long memberId;
    private Long applicantUserId;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> attachments;
    /** 0待审 1通过 2驳回 */
    private Integer status;
    private String auditReason;
    private Long auditUserId;
    private LocalDateTime auditTime;
}
