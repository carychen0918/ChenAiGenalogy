package chen.genealogy.module.genealogy.controller.app.aimatch;

import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.tenant.core.aop.TenantIgnore;
import chen.genealogy.module.genealogy.controller.admin.aimatch.vo.AiMatchChatReqVO;
import chen.genealogy.module.genealogy.controller.admin.aimatch.vo.AiMatchChatRespVO;
import chen.genealogy.module.genealogy.controller.admin.aimatch.vo.AiMatchConversationRespVO;
import chen.genealogy.module.genealogy.controller.admin.aimatch.vo.AiMatchSessionRespVO;
import chen.genealogy.module.genealogy.service.aimatch.AiMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static chen.genealogy.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - AI 寻宗问祖")
@RestController
@RequestMapping("/genealogy/ai-match")
@Validated
public class AppAiMatchController {

    @Resource
    private AiMatchService aiMatchService;

    @PostMapping("/session")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "创建寻宗会话")
    public CommonResult<AiMatchSessionRespVO> createSession() {
        return success(aiMatchService.createSession());
    }

    @GetMapping("/session/get")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "按 sessionId 恢复当前对话")
    public CommonResult<AiMatchSessionRespVO> getSession(
            @RequestParam("sessionId") @NotBlank(message = "会话编号不能为空") String sessionId) {
        return success(aiMatchService.getSession(sessionId));
    }

    @GetMapping("/conversation/list")
    @TenantIgnore
    @Operation(summary = "登录用户的寻宗会话列表")
    public CommonResult<List<AiMatchConversationRespVO>> listConversations() {
        return success(aiMatchService.listConversations());
    }

    @GetMapping("/conversation/get")
    @TenantIgnore
    @Operation(summary = "打开指定寻宗会话")
    public CommonResult<AiMatchSessionRespVO> getConversation(
            @RequestParam("id") @NotNull(message = "会话编号不能为空") Long id) {
        return success(aiMatchService.getConversation(id));
    }

    @PostMapping("/conversation/create")
    @TenantIgnore
    @Operation(summary = "新建寻宗会话")
    public CommonResult<AiMatchSessionRespVO> createConversation() {
        return success(aiMatchService.createSession());
    }

    @DeleteMapping("/conversation/delete")
    @TenantIgnore
    @Operation(summary = "删除寻宗会话")
    @Parameter(name = "id", description = "会话编号", required = true)
    public CommonResult<Boolean> deleteConversation(
            @RequestParam("id") @NotNull(message = "会话编号不能为空") Long id) {
        aiMatchService.deleteConversation(id);
        return success(true);
    }

    @PostMapping("/chat")
    @PermitAll
    @TenantIgnore
    @Operation(summary = "寻宗对话（一次性返回，供小程序/H5 使用）")
    public CommonResult<AiMatchChatRespVO> chat(@Valid @RequestBody AiMatchChatReqVO reqVO) {
        return success(aiMatchService.chat(reqVO));
    }
}
