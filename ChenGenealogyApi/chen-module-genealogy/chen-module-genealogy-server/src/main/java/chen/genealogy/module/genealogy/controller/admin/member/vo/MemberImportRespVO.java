package chen.genealogy.module.genealogy.controller.admin.member.vo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MemberImportRespVO {

    private Integer createCount;
    private Integer updateCount;
    private Integer failureCount;
    @Builder.Default
    private List<String> failureMessages = new ArrayList<>();
}
