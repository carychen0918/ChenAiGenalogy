package chen.genealogy.module.genealogy.dal.dataobject.scholarship;

import chen.genealogy.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@TableName(value = "tb_scholarship_disbursement", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDisbursementDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long applicationId;
    private BigDecimal amount;
    /** 1银行转账 2现金 3其他 */
    private Integer method;
    private LocalDateTime disburseDate;
    private String handlerName;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> voucherUrls;
    private String remark;
}
