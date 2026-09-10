package chen.genealogy.module.genealogy.dal.dataobject.feed;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

@TableName(value = "tb_feed", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    /** 1公告 2动态 3公示 */
    private Integer type;
    private String title;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private Boolean pinned;
    /** 0待审 1已发布 2驳回 3下线 */
    private Integer status;
    private Long authorUserId;
    private String authorName;
    private Integer likeCount;
    private Integer commentCount;
}
