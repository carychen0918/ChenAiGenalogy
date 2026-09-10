package chen.genealogy.module.pay.convert.wallet;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.module.pay.controller.admin.wallet.vo.transaction.PayWalletTransactionRespVO;
import chen.genealogy.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionRespVO;
import chen.genealogy.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import chen.genealogy.module.pay.service.wallet.bo.WalletTransactionCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletTransactionConvert {

    PayWalletTransactionConvert INSTANCE = Mappers.getMapper(PayWalletTransactionConvert.class);

    PageResult<PayWalletTransactionRespVO> convertPage2(PageResult<PayWalletTransactionDO> page);

    PayWalletTransactionDO convert(WalletTransactionCreateReqBO bean);

}
