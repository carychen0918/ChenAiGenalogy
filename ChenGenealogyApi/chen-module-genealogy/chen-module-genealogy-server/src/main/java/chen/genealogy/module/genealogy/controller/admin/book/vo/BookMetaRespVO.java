package chen.genealogy.module.genealogy.controller.admin.book.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "谱书目录与封面摘要")
public class BookMetaRespVO {

    private String bookTitle;
    private String familyName;
    private String surname;
    private String ancestorName;
    private String region;
    private String logo;
    private String bookRevision;
    private String preface;
    private Integer totalPages;
    private Integer firstLineagePage;
    private List<BookTocItemVO> toc = new ArrayList<>();
    private List<BookGenerationIndexVO> generations = new ArrayList<>();
}
