package chen.genealogy.module.mes.dal.mysql.pro.card;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.pro.card.MesProCardProcessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 流转卡工序记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesProCardProcessMapper extends BaseMapperX<MesProCardProcessDO> {

    default PageResult<MesProCardProcessDO> selectPage(MesProCardProcessPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesProCardProcessDO>()
                .eqIfPresent(MesProCardProcessDO::getCardId, reqVO.getCardId())
                .eqIfPresent(MesProCardProcessDO::getProcessId, reqVO.getProcessId())
                .eqIfPresent(MesProCardProcessDO::getWorkstationId, reqVO.getWorkstationId())
                .eqIfPresent(MesProCardProcessDO::getUserId, reqVO.getUserId())
                .orderByAsc(MesProCardProcessDO::getSort));
    }

    default List<MesProCardProcessDO> selectListByCardId(Long cardId) {
        return selectList(MesProCardProcessDO::getCardId, cardId);
    }

    default void deleteByCardId(Long cardId) {
        delete(MesProCardProcessDO::getCardId, cardId);
    }

}
