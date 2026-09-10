package chen.genealogy.module.genealogy.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MigrationNodeSaveReqVO {
    private Long id;
    private String nodeTime;
    private String place;
    private String eventTitle;
    private String person;
    private String description;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @Schema(description = "显示顺序，数字越小越靠前")
    private Integer sort;
}
