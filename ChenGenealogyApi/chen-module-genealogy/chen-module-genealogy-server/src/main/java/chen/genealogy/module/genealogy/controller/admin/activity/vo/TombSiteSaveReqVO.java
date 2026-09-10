package chen.genealogy.module.genealogy.controller.admin.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TombSiteSaveReqVO {
    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String parking;
    private String entrance;
    private String toilet;
    private String remark;
}
