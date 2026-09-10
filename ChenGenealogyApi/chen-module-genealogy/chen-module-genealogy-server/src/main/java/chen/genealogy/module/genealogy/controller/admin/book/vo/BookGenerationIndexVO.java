package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "谱书字辈/世代索引")
public class BookGenerationIndexVO {

    private Integer generationNo;
    @Schema(description = "该世字辈，顿号分隔")
    private String words;
    @Schema(description = "该世正文首页；无成员时指向字辈总表页")
    private Integer pageNo;
    private Integer memberCount;
}
