package chen.genealogy.module.genealogy.controller.admin.showcase.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShowcaseConfigSaveReqVO {
    private Long featuredMemberId;
    private Long featuredDeedId;
    private Long featuredAncestorDeedId;
    private List<Long> wallApplicationIds;
    private List<Long> calendarMemberIds;
}
