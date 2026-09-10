package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "谱书单页")
public class BookPageRespVO {

    private Integer pageNo;
    private Integer totalPages;
    @Schema(description = "COVER / PREFACE / GENERATION / LINEAGE")
    private String type;
    private String title;
    private Integer generationNo;
    private String generationWords;
    /** 世系页未登录时为 true，不返回人物 */
    private Boolean loginRequired;

    private String bookTitle;
    private String familyName;
    private String surname;
    private String ancestorName;
    private String region;
    private String logo;
    private String bookRevision;
    private String html;
    private List<BookGenerationRowVO> generationRows = new ArrayList<>();
    private List<BookBranchVO> branches = new ArrayList<>();
}
