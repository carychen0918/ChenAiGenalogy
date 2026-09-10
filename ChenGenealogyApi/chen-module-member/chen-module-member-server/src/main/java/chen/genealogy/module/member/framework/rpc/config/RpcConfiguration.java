package chen.genealogy.module.member.framework.rpc.config;

import chen.genealogy.module.system.api.logger.LoginLogApi;
import chen.genealogy.module.system.api.sms.SmsCodeApi;
import chen.genealogy.module.system.api.social.SocialClientApi;
import chen.genealogy.module.system.api.social.SocialUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "memberRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {SmsCodeApi.class, LoginLogApi.class, SocialUserApi.class, SocialClientApi.class})
public class RpcConfiguration {
}
