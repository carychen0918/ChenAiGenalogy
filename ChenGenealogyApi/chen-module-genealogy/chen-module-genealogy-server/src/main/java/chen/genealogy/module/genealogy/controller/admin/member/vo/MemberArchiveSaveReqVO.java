package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 成员档案维护")
@Data
public class MemberArchiveSaveReqVO {

    @NotNull(message = "成员编号不能为空")
    private Long id;
    @Schema(description = "生平简介")
    private String intro;
    @Schema(description = "照片集")
    private List<String> photoUrls;
}
