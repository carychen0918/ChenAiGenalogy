package chen.genealogy.module.mes.dal.mysql.cal.plan;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.cal.plan.vo.shift.MesCalPlanShiftPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 计划班次 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesCalPlanShiftMapper extends BaseMapperX<MesCalPlanShiftDO> {

    default PageResult<MesCalPlanShiftDO> selectPage(MesCalPlanShiftPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesCalPlanShiftDO>()
                .eqIfPresent(MesCalPlanShiftDO::getPlanId, reqVO.getPlanId())
                .likeIfPresent(MesCalPlanShiftDO::getName, reqVO.getName())
                .orderByAsc(MesCalPlanShiftDO::getSort));
    }

    default List<MesCalPlanShiftDO> selectListByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<MesCalPlanShiftDO>()
                .eq(MesCalPlanShiftDO::getPlanId, planId)
                .orderByAsc(MesCalPlanShiftDO::getSort));
    }

    default Long selectCountByPlanId(Long planId) {
        return selectCount(MesCalPlanShiftDO::getPlanId, planId);
    }

    default void deleteByPlanId(Long planId) {
        delete(new LambdaQueryWrapperX<MesCalPlanShiftDO>()
                .eq(MesCalPlanShiftDO::getPlanId, planId));
    }

}
