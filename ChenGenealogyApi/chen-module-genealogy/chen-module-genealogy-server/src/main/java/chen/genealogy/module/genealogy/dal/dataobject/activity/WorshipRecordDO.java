package chen.genealogy.module.genealogy.dal.dataobject.activity;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

@TableName(value = "tb_worship_record", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorshipRecordDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Long userId;
    private String userName;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private Boolean pinned;
    private Boolean online;
    private String ancestorName;
}
