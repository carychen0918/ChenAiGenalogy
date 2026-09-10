package chen.genealogy.module.genealogy.dal.dataobject.member;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_member_deed")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDeedDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long memberId;
    private String title;
    private String content;
    private String source;
    private String occurYear;
}
