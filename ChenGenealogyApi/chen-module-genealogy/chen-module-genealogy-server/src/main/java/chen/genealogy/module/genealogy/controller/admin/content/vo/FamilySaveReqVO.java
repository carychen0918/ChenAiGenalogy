package chen.genealogy.module.genealogy.controller.admin.content.vo;

import lombok.Data;

@Data
public class FamilySaveReqVO {
    private Long id;
    private String name;
    private String surname;
    private String ancestorName;
    private Long ancestorId;
    private String region;
    private String intro;
    private String originContent;
    private String bookTitle;
    private String bookRevision;
    private String bookPreface;
    private Long patriarchUserId;
    private String logo;
}
