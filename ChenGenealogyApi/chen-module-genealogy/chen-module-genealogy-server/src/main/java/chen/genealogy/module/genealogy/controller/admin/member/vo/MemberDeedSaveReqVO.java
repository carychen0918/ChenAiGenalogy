package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "当前登录人 - 事迹荣誉新增/修改")
@Data
public class MemberDeedSaveReqVO {

    private Long id;
    @Schema(description = "成员编号，管理端必填；本人接口由服务端填充")
    private Long memberId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String source;
    private String occurYear;
}
