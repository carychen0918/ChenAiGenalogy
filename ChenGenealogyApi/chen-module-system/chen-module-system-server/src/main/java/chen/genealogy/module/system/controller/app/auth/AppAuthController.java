package chen.genealogy.module.system.controller.app.auth;

import cn.hutool.core.util.StrUtil;
import chen.genealogy.framework.common.enums.UserTypeEnum;
import chen.genealogy.framework.common.pojo.CommonResult;
import chen.genealogy.framework.common.util.object.BeanUtils;
import chen.genealogy.framework.datapermission.core.annotation.DataPermission;
import chen.genealogy.framework.security.config.SecurityProperties;
import chen.genealogy.framework.security.core.util.SecurityFrameworkUtils;
import chen.genealogy.module.system.controller.app.auth.vo.AppAuthLoginReqVO;
import chen.genealogy.module.system.controller.app.auth.vo.AppAuthLoginRespVO;
import chen.genealogy.module.system.controller.app.auth.vo.AppAuthPermissionInfoRespVO;
import chen.genealogy.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import chen.genealogy.module.system.dal.dataobject.user.AdminUserDO;
import chen.genealogy.module.system.enums.oauth2.OAuth2ClientConstants;
import chen.genealogy.module.system.service.auth.AdminAuthService;
import chen.genealogy.module.system.service.oauth2.OAuth2TokenService;
import chen.genealogy.module.system.service.user.AdminUserService;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static chen.genealogy.framework.common.exception.util.ServiceExceptionUtil.exception;
import static chen.genealogy.framework.common.pojo.CommonResult.success;
import static chen.genealogy.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static chen.genealogy.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_CAPTCHA_CODE_ERROR;

@Tag(name = "用户 App - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
public class AppAuthController {

    @Resource
    private AdminAuthService adminAuthService;
    @Resource
    private AdminUserService userService;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private SecurityProperties securityProperties;

    @Value("${chen.captcha.enable:true}")
    private Boolean captchaEnable;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用账号密码登录")
    public CommonResult<AppAuthLoginRespVO> login(@RequestBody @Valid AppAuthLoginReqVO reqVO) {
        validateCaptcha(reqVO.getCaptchaVerification());
        AdminUserDO user = adminAuthService.authenticate(reqVO.getUsername(), reqVO.getPassword());
        OAuth2AccessTokenDO accessToken = oauth2TokenService.createAccessToken(
                user.getId(), UserTypeEnum.MEMBER.getValue(), OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
        return success(BeanUtils.toBean(accessToken, AppAuthLoginRespVO.class));
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            oauth2TokenService.removeAccessToken(token);
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<AppAuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        OAuth2AccessTokenDO accessToken = oauth2TokenService.refreshAccessToken(
                refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return success(BeanUtils.toBean(accessToken, AppAuthLoginRespVO.class));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户信息")
    @DataPermission(enable = false)
    public CommonResult<AppAuthPermissionInfoRespVO> getPermissionInfo() {
        AdminUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return success(null);
        }
        return success(AppAuthPermissionInfoRespVO.builder()
                .user(AppAuthPermissionInfoRespVO.UserVO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .username(user.getUsername())
                        .deptId(user.getDeptId())
                        .build())
                .build());
    }

    private void validateCaptcha(String captchaVerification) {
        if (Boolean.FALSE.equals(captchaEnable)) {
            return;
        }
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if (!response.isSuccess()) {
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }
}
