package chen.genealogy.module.genealogy.controller.admin.feed.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeedCommentPageReqVO extends PageParam {
    private Long feedId;
    private Integer status;
}
