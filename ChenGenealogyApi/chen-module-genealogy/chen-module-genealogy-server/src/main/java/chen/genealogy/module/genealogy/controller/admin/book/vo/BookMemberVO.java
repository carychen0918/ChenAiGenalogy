package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "谱书世系人物")
public class BookMemberVO {

    private Long id;
    private String name;
    private Integer gender;
    private Integer generationNo;
    private String generationWord;
    private String fatherName;
    private String motherName;
    private String spouseNames;
    @Schema(description = "生卒，如 1760—1832 或 1990—今")
    private String lifeSpan;
    private String intro;
    private Boolean alive;
    private String tags;
    private List<String> photoUrls;
    private List<BookDeedVO> deeds;
}
