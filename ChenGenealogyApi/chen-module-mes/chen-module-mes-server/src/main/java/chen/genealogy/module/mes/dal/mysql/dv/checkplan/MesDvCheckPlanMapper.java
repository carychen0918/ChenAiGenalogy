package chen.genealogy.module.mes.dal.mysql.dv.checkplan;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 点检保养方案 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesDvCheckPlanMapper extends BaseMapperX<MesDvCheckPlanDO> {

    default PageResult<MesDvCheckPlanDO> selectPage(MesDvCheckPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvCheckPlanDO>()
                .likeIfPresent(MesDvCheckPlanDO::getCode, reqVO.getCode())
                .likeIfPresent(MesDvCheckPlanDO::getName, reqVO.getName())
                .eqIfPresent(MesDvCheckPlanDO::getType, reqVO.getType())
                .eqIfPresent(MesDvCheckPlanDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesDvCheckPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesDvCheckPlanDO::getId));
    }

    default MesDvCheckPlanDO selectByCode(String code) {
        return selectOne(MesDvCheckPlanDO::getCode, code);
    }

}
