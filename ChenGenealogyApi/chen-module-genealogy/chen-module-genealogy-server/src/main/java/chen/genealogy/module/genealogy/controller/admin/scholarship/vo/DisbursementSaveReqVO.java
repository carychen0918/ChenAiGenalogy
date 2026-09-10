package chen.genealogy.module.genealogy.controller.admin.scholarship.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DisbursementSaveReqVO {
    @NotNull
    private Long applicationId;
    @NotNull(message = "发放金额不能为空")
    private BigDecimal amount;
    @NotNull
    private Integer method;
    @NotNull
    private LocalDateTime disburseDate;
    @NotNull
    private String handlerName;
    private List<String> voucherUrls;
    private String remark;
}
