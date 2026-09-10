package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "谱书人物事迹荣誉")
public class BookDeedVO {

    private Long id;
    private String title;
    private String content;
    private String source;
    private String occurYear;
}
