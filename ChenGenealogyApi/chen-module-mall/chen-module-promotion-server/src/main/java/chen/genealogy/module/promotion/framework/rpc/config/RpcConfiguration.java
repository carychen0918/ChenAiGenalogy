package chen.genealogy.module.promotion.framework.rpc.config;

import chen.genealogy.module.infra.api.websocket.WebSocketSenderApi;
import chen.genealogy.module.member.api.user.MemberUserApi;
import chen.genealogy.module.product.api.category.ProductCategoryApi;
import chen.genealogy.module.product.api.sku.ProductSkuApi;
import chen.genealogy.module.product.api.spu.ProductSpuApi;
import chen.genealogy.module.system.api.social.SocialClientApi;
import chen.genealogy.module.system.api.user.AdminUserApi;
import chen.genealogy.module.trade.api.order.TradeOrderApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "promotionRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ProductSkuApi.class, ProductSpuApi.class, ProductCategoryApi.class,
        MemberUserApi.class, TradeOrderApi.class, AdminUserApi.class, SocialClientApi.class,
        WebSocketSenderApi.class})
public class RpcConfiguration {
}
