package chen.genealogy.module.genealogy.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 新增成员结果")
@Data
public class MemberCreateRespVO {

    private Long id;
    @Schema(description = "同步创建的登录账号，未开通时为空")
    private String username;
    @Schema(description = "默认密码，仅新开通账号时返回")
    private String defaultPassword;
}
