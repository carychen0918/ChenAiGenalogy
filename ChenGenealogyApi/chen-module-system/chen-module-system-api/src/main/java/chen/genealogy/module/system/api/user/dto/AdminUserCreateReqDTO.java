package chen.genealogy.module.system.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "RPC 服务 - 创建用户 Request DTO")
@Data
public class AdminUserCreateReqDTO {

    @NotBlank(message = "用户账号不能为空")
    private String username;
    private String nickname;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String mobile;
    private Integer sex;
    private String avatar;
}
