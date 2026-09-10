package chen.genealogy.module.mes.dal.mysql.wm.returnsales;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 销售退货单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmReturnSalesMapper extends BaseMapperX<MesWmReturnSalesDO> {

    default PageResult<MesWmReturnSalesDO> selectPage(MesWmReturnSalesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmReturnSalesDO>()
                .likeIfPresent(MesWmReturnSalesDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmReturnSalesDO::getName, reqVO.getName())
                .likeIfPresent(MesWmReturnSalesDO::getSalesOrderCode, reqVO.getSalesOrderCode())
                .eqIfPresent(MesWmReturnSalesDO::getClientId, reqVO.getClientId())
                .eqIfPresent(MesWmReturnSalesDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesWmReturnSalesDO::getReturnDate, reqVO.getReturnDate())
                .orderByDesc(MesWmReturnSalesDO::getId));
    }

    default MesWmReturnSalesDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<MesWmReturnSalesDO>()
                .eq(MesWmReturnSalesDO::getCode, code));
    }

}
