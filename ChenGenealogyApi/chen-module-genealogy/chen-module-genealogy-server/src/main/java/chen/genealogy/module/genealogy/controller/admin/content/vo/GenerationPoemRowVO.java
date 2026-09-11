package chen.genealogy.module.genealogy.controller.admin.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字辈派语对照表一行")
public class GenerationPoemRowVO {

    @Schema(description = "世代数")
    private Integer generationNo;
    @Schema(description = "赤土官庄世序")
    private String chituOrder;
    @Schema(description = "全国统一字派")
    private String nationalSource;
    @Schema(description = "高安椒坊字派")
    private String jiaofangSource;
    @Schema(description = "长房")
    private String house1;
    @Schema(description = "二房")
    private String house2;
    @Schema(description = "三房")
    private String house3;
    @Schema(description = "三房织金")
    private String house3Zhijin;
    @Schema(description = "四五房")
    private String house45;
}
