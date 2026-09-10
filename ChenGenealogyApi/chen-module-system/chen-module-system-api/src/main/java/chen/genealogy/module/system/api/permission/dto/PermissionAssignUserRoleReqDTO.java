package chen.genealogy.module.system.api.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "RPC 服务 - 赋予用户角色 Request DTO")
@Data
public class PermissionAssignUserRoleReqDTO {

    @NotNull(message = "用户编号不能为空")
    private Long userId;
    @NotEmpty(message = "角色编号不能为空")
    private Set<Long> roleIds;
}
