package chen.genealogy.module.genealogy.dal.dataobject.feed;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_feed_like")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedLikeDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long feedId;
    private Long userId;
}
