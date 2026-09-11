package chen.genealogy.module.genealogy.controller.admin.content.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerationSaveReqVO {

    private Long id;
    @NotNull(message = "世代不能为空")
    private Integer generationNo;
    @NotBlank(message = "字辈不能为空")
    private String word;
    private String nationalSource;
    private String jiaofangSource;
    private String house;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private String remark;
    private Integer sort;
}
