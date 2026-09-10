package chen.genealogy.module.member.convert.auth;

import chen.genealogy.module.member.controller.app.auth.vo.*;
import chen.genealogy.module.member.controller.app.social.vo.AppSocialUserUnbindReqVO;
import chen.genealogy.module.member.controller.app.user.vo.AppMemberUserResetPasswordReqVO;
import chen.genealogy.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import chen.genealogy.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import chen.genealogy.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import chen.genealogy.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import chen.genealogy.module.system.api.social.dto.SocialUserBindReqDTO;
import chen.genealogy.module.system.api.social.dto.SocialUserUnbindReqDTO;
import chen.genealogy.module.system.api.social.dto.SocialWxJsapiSignatureRespDTO;
import chen.genealogy.module.system.enums.sms.SmsSceneEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    SocialUserBindReqDTO convert(Long userId, Integer userType, AppAuthSocialLoginReqVO reqVO);
    SocialUserUnbindReqDTO convert(Long userId, Integer userType, AppSocialUserUnbindReqVO reqVO);

    SmsCodeSendReqDTO convert(AppAuthSmsSendReqVO reqVO);
    SmsCodeUseReqDTO convert(AppMemberUserResetPasswordReqVO reqVO, SmsSceneEnum scene, String usedIp);
    SmsCodeUseReqDTO convert(AppAuthSmsLoginReqVO reqVO, Integer scene, String usedIp);

    AppAuthLoginRespVO convert(OAuth2AccessTokenRespDTO bean, String openid);

    SmsCodeValidateReqDTO convert(AppAuthSmsValidateReqVO bean);

    SocialWxJsapiSignatureRespDTO convert(SocialWxJsapiSignatureRespDTO bean);

}
