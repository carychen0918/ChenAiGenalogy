package chen.genealogy.module.genealogy.controller.admin.scholarship.vo;

import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScholarshipSaveReqVO {
    private Long id;
    @NotBlank
    private String school;
    @NotBlank
    private String major;
    @NotBlank
    private String grade;
    @NotBlank
    private String studentNo;
    @NotNull
    private Integer type;
    private BigDecimal suggestAmount;
    @NotBlank
    private String familySituation;
    private List<ScholarshipApplicationDO.ApplicationMaterial> materials;
}
