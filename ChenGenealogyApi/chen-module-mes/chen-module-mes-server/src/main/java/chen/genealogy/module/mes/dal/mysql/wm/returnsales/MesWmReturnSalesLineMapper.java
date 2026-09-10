package chen.genealogy.module.mes.dal.mysql.wm.returnsales;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.returnsales.vo.line.MesWmReturnSalesLinePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 销售退货单行 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmReturnSalesLineMapper extends BaseMapperX<MesWmReturnSalesLineDO> {

    default PageResult<MesWmReturnSalesLineDO> selectPage(MesWmReturnSalesLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmReturnSalesLineDO>()
                .eqIfPresent(MesWmReturnSalesLineDO::getReturnId, reqVO.getReturnId())
                .eqIfPresent(MesWmReturnSalesLineDO::getItemId, reqVO.getItemId())
                .orderByDesc(MesWmReturnSalesLineDO::getId));
    }

    default List<MesWmReturnSalesLineDO> selectListByReturnId(Long returnId) {
        return selectList(new LambdaQueryWrapperX<MesWmReturnSalesLineDO>()
                .eq(MesWmReturnSalesLineDO::getReturnId, returnId));
    }

    default void deleteByReturnId(Long returnId) {
        delete(new LambdaQueryWrapperX<MesWmReturnSalesLineDO>()
                .eq(MesWmReturnSalesLineDO::getReturnId, returnId));
    }

}
