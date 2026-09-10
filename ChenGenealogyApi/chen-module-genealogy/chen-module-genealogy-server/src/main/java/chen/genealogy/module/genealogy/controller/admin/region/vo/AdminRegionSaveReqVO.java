package chen.genealogy.module.genealogy.controller.admin.region.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRegionSaveReqVO {
    @NotNull(message = "用户不能为空")
    private Long userId;
    @NotNull(message = "请选择管辖地区")
    private Integer areaId;
}
