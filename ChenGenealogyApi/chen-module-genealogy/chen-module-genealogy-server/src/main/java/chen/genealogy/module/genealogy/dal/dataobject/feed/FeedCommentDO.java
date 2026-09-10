package chen.genealogy.module.genealogy.dal.dataobject.feed;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_feed_comment")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedCommentDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long feedId;
    private Long userId;
    private String userName;
    private String content;
    /** 0待审 1通过 2驳回 */
    private Integer status;
}
