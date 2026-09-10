package chen.genealogy.module.mes.dal.mysql.wm.returnissue;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.returnissue.vo.MesWmReturnIssuePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 生产退料单 Mapper
 */
@Mapper
public interface MesWmReturnIssueMapper extends BaseMapperX<MesWmReturnIssueDO> {

    default PageResult<MesWmReturnIssueDO> selectPage(MesWmReturnIssuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmReturnIssueDO>()
                .likeIfPresent(MesWmReturnIssueDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmReturnIssueDO::getName, reqVO.getName())
                .eqIfPresent(MesWmReturnIssueDO::getWorkstationId, reqVO.getWorkstationId())
                .eqIfPresent(MesWmReturnIssueDO::getWorkOrderId, reqVO.getWorkOrderId())
                .eqIfPresent(MesWmReturnIssueDO::getType, reqVO.getType())
                .orderByDesc(MesWmReturnIssueDO::getId));
    }

    default MesWmReturnIssueDO selectByCode(String code) {
        return selectOne(MesWmReturnIssueDO::getCode, code);
    }

}
