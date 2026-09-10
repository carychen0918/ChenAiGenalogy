package chen.genealogy.module.genealogy.dal.dataobject.activity;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("tb_activity_registration")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRegistrationDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Long userId;
    private Long memberId;
    private String userName;
    private String mobile;
    private Integer peopleCount;
    private Boolean needBus;
    /** 1已报名 2候补 3已取消 */
    private Integer status;
    private Boolean reminded;
}
