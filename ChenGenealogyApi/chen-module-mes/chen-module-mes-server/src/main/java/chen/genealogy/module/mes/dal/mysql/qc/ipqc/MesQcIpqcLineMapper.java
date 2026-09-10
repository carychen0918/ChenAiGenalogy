package chen.genealogy.module.mes.dal.mysql.qc.ipqc;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.qc.ipqc.vo.line.MesQcIpqcLinePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 过程检验单行 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesQcIpqcLineMapper extends BaseMapperX<MesQcIpqcLineDO> {

    default PageResult<MesQcIpqcLineDO> selectPage(MesQcIpqcLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcIpqcLineDO>()
                .eqIfPresent(MesQcIpqcLineDO::getIpqcId, reqVO.getIpqcId())
                .orderByAsc(MesQcIpqcLineDO::getId));
    }

    default List<MesQcIpqcLineDO> selectListByIpqcId(Long ipqcId) {
        return selectList(MesQcIpqcLineDO::getIpqcId, ipqcId);
    }

    default void deleteByIpqcId(Long ipqcId) {
        delete(new LambdaQueryWrapperX<MesQcIpqcLineDO>()
                .eq(MesQcIpqcLineDO::getIpqcId, ipqcId));
    }

    default Long selectCountByUnitMeasureId(Long unitMeasureId) {
        return selectCount(MesQcIpqcLineDO::getUnitMeasureId, unitMeasureId);
    }

}
