package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字辈总表一行")
public class BookGenerationRowVO {

    private Integer generationNo;
    private String words;
    private String remark;
}
