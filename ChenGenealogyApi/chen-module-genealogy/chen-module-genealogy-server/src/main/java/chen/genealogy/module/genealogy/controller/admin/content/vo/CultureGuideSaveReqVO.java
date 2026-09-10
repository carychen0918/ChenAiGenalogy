package chen.genealogy.module.genealogy.controller.admin.content.vo;

import lombok.Data;

@Data
public class CultureGuideSaveReqVO {
    private Long id;
    private String title;
    private String content;
    private Integer sort;
    private Integer status;
}
