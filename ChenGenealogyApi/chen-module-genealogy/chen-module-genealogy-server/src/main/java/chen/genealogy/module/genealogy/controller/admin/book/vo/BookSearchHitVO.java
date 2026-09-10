package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "谱书定位结果")
public class BookSearchHitVO {

    private Long memberId;
    private String name;
    private Integer generationNo;
    private String generationWord;
    private String fatherName;
    private Integer pageNo;
    @Schema(description = "GENERATION / LINEAGE")
    private String type;
    private String title;
}
