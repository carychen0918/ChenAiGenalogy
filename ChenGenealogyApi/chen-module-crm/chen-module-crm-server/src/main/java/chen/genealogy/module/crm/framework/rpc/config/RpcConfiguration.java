package chen.genealogy.module.crm.framework.rpc.config;

import chen.genealogy.module.bpm.api.task.BpmProcessInstanceApi;
import chen.genealogy.module.system.api.dept.DeptApi;
import chen.genealogy.module.system.api.dept.PostApi;
import chen.genealogy.module.system.api.logger.OperateLogApi;
import chen.genealogy.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "crmRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, DeptApi.class, PostApi.class,
        OperateLogApi.class,
        BpmProcessInstanceApi.class})
public class RpcConfiguration {
}
