package chen.genealogy.module.genealogy.controller.admin.content.vo;

import lombok.Data;

@Data
public class AncestorDeedSaveReqVO {
    private Long id;
    private Long memberId;
    private String name;
    private String title;
    private String category;
    private String source;
    private String content;
    private String coverUrl;
}
