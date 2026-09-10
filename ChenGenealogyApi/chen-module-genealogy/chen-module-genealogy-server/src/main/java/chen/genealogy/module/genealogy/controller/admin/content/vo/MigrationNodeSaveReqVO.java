package chen.genealogy.module.genealogy.controller.admin.content.vo;

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
    private Integer sort;
}
