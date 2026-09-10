package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "谱书一支（同一父亲下的子女）")
public class BookBranchVO {

    @Schema(description = "支系标题，如 陈永昌之子")
    private String title;
    private Long fatherId;
    private String fatherName;
    @Schema(description = "该支字辈")
    private String generationWord;
    private List<BookMemberVO> members;
}
