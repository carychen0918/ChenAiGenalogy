package chen.genealogy.module.genealogy.controller.admin.member.vo;

import chen.genealogy.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArchiveApplyPageReqVO extends PageParam {

    private Integer status;
    private Long memberId;
}
