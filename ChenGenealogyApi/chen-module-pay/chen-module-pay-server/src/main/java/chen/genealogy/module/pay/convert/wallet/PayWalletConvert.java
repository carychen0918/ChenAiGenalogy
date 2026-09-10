package chen.genealogy.module.pay.convert.wallet;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.pay.controller.admin.wallet.vo.wallet.PayWalletRespVO;
import chen.genealogy.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import chen.genealogy.module.pay.dal.dataobject.wallet.PayWalletDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletConvert {

    PayWalletConvert INSTANCE = Mappers.getMapper(PayWalletConvert.class);

    AppPayWalletRespVO convert(PayWalletDO bean);

    PayWalletRespVO convert02(PayWalletDO bean);

    PageResult<PayWalletRespVO> convertPage(PageResult<PayWalletDO> page);

}
