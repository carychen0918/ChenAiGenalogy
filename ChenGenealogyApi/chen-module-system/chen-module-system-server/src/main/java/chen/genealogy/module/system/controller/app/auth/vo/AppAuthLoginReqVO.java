package chen.genealogy.module.system.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Schema(description = "用户 App - 账号密码登录 Request VO")
@Data
public class AppAuthLoginReqVO {

    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "chenyuanma")
    @NotEmpty(message = "登录账号不能为空")
    @Length(min = 4, max = 30, message = "账号长度为 4-30 位")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "buzhidao")
    @NotEmpty(message = "密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;

    @Schema(description = "验证码，验证码开启时需要传递")
    private String captchaVerification;
}
