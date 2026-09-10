package chen.genealogy.module.mes.framework.rpc.config;

import chen.genealogy.module.system.api.dept.PostApi;
import chen.genealogy.module.system.api.dict.DictDataApi;
import chen.genealogy.module.system.api.permission.RoleApi;
import chen.genealogy.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "mesRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, PostApi.class, RoleApi.class, DictDataApi.class})
public class RpcConfiguration {
}
