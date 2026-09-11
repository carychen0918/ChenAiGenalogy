package chen.genealogy.module.genealogy.controller.admin.content.vo;

import lombok.Data;

@Data
public class GenerationRespVO {

    private Long id;
    private Long familyId;
    private Integer generationNo;
    private String word;
    private String nationalSource;
    private String jiaofangSource;
    private String house;
    private Integer status;
    private String remark;
    private Integer sort;
}
