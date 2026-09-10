package chen.genealogy.module.genealogy.controller.admin.activity.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityPageReqVO extends PageParam {
    private String title;
    private Integer status;
}
