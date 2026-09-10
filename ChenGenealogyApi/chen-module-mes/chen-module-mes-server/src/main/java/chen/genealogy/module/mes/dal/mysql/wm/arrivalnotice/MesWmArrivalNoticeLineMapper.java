package chen.genealogy.module.mes.dal.mysql.wm.arrivalnotice;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLinePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 到货通知单行 Mapper
 */
@Mapper
public interface MesWmArrivalNoticeLineMapper extends BaseMapperX<MesWmArrivalNoticeLineDO> {

    default PageResult<MesWmArrivalNoticeLineDO> selectPage(MesWmArrivalNoticeLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmArrivalNoticeLineDO>()
                .eqIfPresent(MesWmArrivalNoticeLineDO::getNoticeId, reqVO.getNoticeId())
                .orderByDesc(MesWmArrivalNoticeLineDO::getId));
    }

    default List<MesWmArrivalNoticeLineDO> selectListByNoticeId(Long noticeId) {
        return selectList(MesWmArrivalNoticeLineDO::getNoticeId, noticeId);
    }

    default void deleteByNoticeId(Long noticeId) {
        delete(MesWmArrivalNoticeLineDO::getNoticeId, noticeId);
    }

    default List<MesWmArrivalNoticeLineDO> selectListByIqcPending(List<Long> noticeIds) {
        return selectList(new LambdaQueryWrapperX<MesWmArrivalNoticeLineDO>()
                .in(MesWmArrivalNoticeLineDO::getNoticeId, noticeIds)
                .eq(MesWmArrivalNoticeLineDO::getIqcCheckFlag, true)
                .isNull(MesWmArrivalNoticeLineDO::getIqcId));
    }

}
