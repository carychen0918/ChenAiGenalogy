package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "谱书目录项")
public class BookTocItemVO {

    @Schema(description = "页码，从 1 起")
    private Integer pageNo;
    @Schema(description = "COVER / PREFACE / GENERATION / LINEAGE")
    private String type;
    @Schema(description = "目录标题")
    private String title;
    @Schema(description = "世代，仅世系页")
    private Integer generationNo;
}
