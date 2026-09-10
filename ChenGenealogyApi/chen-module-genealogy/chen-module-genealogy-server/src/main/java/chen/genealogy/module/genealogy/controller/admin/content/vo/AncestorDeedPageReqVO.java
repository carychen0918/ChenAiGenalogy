package chen.genealogy.module.genealogy.controller.admin.content.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AncestorDeedPageReqVO extends PageParam {

    private Long familyId;
    private String category;
    private String name;
}
