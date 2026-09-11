package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字辈总表一行")
public class BookGenerationRowVO {

    private Integer generationNo;
    private String chituOrder;
    private String words;
    private String nationalSource;
    private String jiaofangSource;
    private String house1;
    private String house2;
    private String house3;
    private String house3Zhijin;
    private String house45;
    private String remark;
}
