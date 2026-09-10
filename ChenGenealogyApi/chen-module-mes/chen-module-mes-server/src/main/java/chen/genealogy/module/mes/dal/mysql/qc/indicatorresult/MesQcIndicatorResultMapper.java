package chen.genealogy.module.mes.dal.mysql.qc.indicatorresult;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 检验结果记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcIndicatorResultMapper extends BaseMapperX<MesQcIndicatorResultDO> {

    default PageResult<MesQcIndicatorResultDO> selectPage(MesQcIndicatorResultPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcIndicatorResultDO>()
                .eqIfPresent(MesQcIndicatorResultDO::getQcId, reqVO.getQcId())
                .eqIfPresent(MesQcIndicatorResultDO::getQcType, reqVO.getQcType())
                .likeIfPresent(MesQcIndicatorResultDO::getCode, reqVO.getCode())
                .eqIfPresent(MesQcIndicatorResultDO::getItemId, reqVO.getItemId())
                .orderByDesc(MesQcIndicatorResultDO::getId));
    }

    default Long selectCountByQcIdAndType(Long qcId, Integer qcType) {
        return selectCount(new LambdaQueryWrapperX<MesQcIndicatorResultDO>()
                .eq(MesQcIndicatorResultDO::getQcId, qcId)
                .eq(MesQcIndicatorResultDO::getQcType, qcType));
    }

}
