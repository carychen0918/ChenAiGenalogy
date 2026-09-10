package chen.genealogy.module.genealogy.controller.admin.aimatch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "公开 - AI 寻宗对话")
@Data
public class AiMatchChatReqVO {

    @NotBlank(message = "会话编号不能为空")
    private String sessionId;
    @NotBlank(message = "请描述您的家族线索")
    private String content;
    @Schema(description = "浏览器身份（未登录限流用）")
    private String clientId;
}
