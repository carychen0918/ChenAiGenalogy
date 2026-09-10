package chen.genealogy.module.system.framework.rpc.config;

import chen.genealogy.module.infra.api.config.ConfigApi;
import chen.genealogy.module.infra.api.file.FileApi;
import chen.genealogy.module.infra.api.websocket.WebSocketSenderApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "systemRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {FileApi.class, WebSocketSenderApi.class, ConfigApi.class})
public class RpcConfiguration {
}
