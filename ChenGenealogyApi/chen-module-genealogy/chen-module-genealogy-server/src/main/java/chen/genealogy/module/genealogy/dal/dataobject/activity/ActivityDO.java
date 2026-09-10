package chen.genealogy.module.genealogy.dal.dataobject.activity;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("tb_activity")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long familyId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String place;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String processDesc;
    private String gatherPlace;
    private String notice;
    private LocalDateTime deadline;
    private Integer maxCount;
    private String contactName;
    private String contactMobile;
    /** 0草稿 1报名中 2已结束 3已取消 */
    private Integer status;
    private Boolean reminded;

    @TableField(exist = false)
    private Long joinedCount;
    /** 当前用户报名状态：1已报名 2候补，未报名则为空 */
    @TableField(exist = false)
    private Integer myRegStatus;
}
