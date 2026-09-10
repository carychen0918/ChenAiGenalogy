package chen.genealogy.module.im.framework.rpc.config;

import chen.genealogy.module.infra.api.websocket.WebSocketSenderApi;
import chen.genealogy.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "imRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, WebSocketSenderApi.class})
public class RpcConfiguration {
}
