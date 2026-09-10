package chen.genealogy.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 登录用户信息 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAuthPermissionInfoRespVO {

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(description = "用户信息")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserVO {
        private Long id;
        private String nickname;
        private String avatar;
        private String username;
        private Long deptId;
    }
}
