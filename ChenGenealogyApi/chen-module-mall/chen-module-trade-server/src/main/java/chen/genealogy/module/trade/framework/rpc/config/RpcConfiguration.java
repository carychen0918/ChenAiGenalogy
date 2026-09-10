package chen.genealogy.module.trade.framework.rpc.config;

import chen.genealogy.module.member.api.address.MemberAddressApi;
import chen.genealogy.module.member.api.config.MemberConfigApi;
import chen.genealogy.module.member.api.level.MemberLevelApi;
import chen.genealogy.module.member.api.point.MemberPointApi;
import chen.genealogy.module.member.api.user.MemberUserApi;
import chen.genealogy.module.pay.api.order.PayOrderApi;
import chen.genealogy.module.pay.api.refund.PayRefundApi;
import chen.genealogy.module.pay.api.transfer.PayTransferApi;
import chen.genealogy.module.pay.api.wallet.PayWalletApi;
import chen.genealogy.module.product.api.category.ProductCategoryApi;
import chen.genealogy.module.product.api.comment.ProductCommentApi;
import chen.genealogy.module.product.api.sku.ProductSkuApi;
import chen.genealogy.module.product.api.spu.ProductSpuApi;
import chen.genealogy.module.promotion.api.bargain.BargainActivityApi;
import chen.genealogy.module.promotion.api.bargain.BargainRecordApi;
import chen.genealogy.module.promotion.api.combination.CombinationRecordApi;
import chen.genealogy.module.promotion.api.coupon.CouponApi;
import chen.genealogy.module.promotion.api.discount.DiscountActivityApi;
import chen.genealogy.module.promotion.api.point.PointActivityApi;
import chen.genealogy.module.promotion.api.reward.RewardActivityApi;
import chen.genealogy.module.promotion.api.seckill.SeckillActivityApi;
import chen.genealogy.module.system.api.notify.NotifyMessageSendApi;
import chen.genealogy.module.system.api.social.SocialClientApi;
import chen.genealogy.module.system.api.social.SocialUserApi;
import chen.genealogy.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "tradeRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {
        BargainActivityApi.class, BargainRecordApi.class, CombinationRecordApi.class,
        CouponApi.class, DiscountActivityApi.class, RewardActivityApi.class, SeckillActivityApi.class, PointActivityApi.class,
        MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigApi.class,
        ProductSpuApi.class, ProductSkuApi.class, ProductCommentApi.class, ProductCategoryApi.class,
        PayOrderApi.class, PayRefundApi.class, PayTransferApi.class, PayWalletApi.class,
        AdminUserApi.class, NotifyMessageSendApi.class, SocialClientApi.class, SocialUserApi.class
})
public class RpcConfiguration {
}
