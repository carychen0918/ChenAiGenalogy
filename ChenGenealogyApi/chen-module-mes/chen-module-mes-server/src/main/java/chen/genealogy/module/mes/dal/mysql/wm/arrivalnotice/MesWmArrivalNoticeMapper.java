package chen.genealogy.module.mes.dal.mysql.wm.arrivalnotice;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticePageReqVO;
import chen.genealogy.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 到货通知单 Mapper
 */
@Mapper
public interface MesWmArrivalNoticeMapper extends BaseMapperX<MesWmArrivalNoticeDO> {

    default PageResult<MesWmArrivalNoticeDO> selectPage(MesWmArrivalNoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmArrivalNoticeDO>()
                .likeIfPresent(MesWmArrivalNoticeDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmArrivalNoticeDO::getName, reqVO.getName())
                .likeIfPresent(MesWmArrivalNoticeDO::getPurchaseOrderCode, reqVO.getPurchaseOrderCode())
                .eqIfPresent(MesWmArrivalNoticeDO::getVendorId, reqVO.getVendorId())
                .betweenIfPresent(MesWmArrivalNoticeDO::getArrivalDate, reqVO.getArrivalDate())
                .eqIfPresent(MesWmArrivalNoticeDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesWmArrivalNoticeDO::getId));
    }

    default MesWmArrivalNoticeDO selectByCode(String code) {
        return selectOne(MesWmArrivalNoticeDO::getCode, code);
    }

    default Long selectCountByVendorId(Long vendorId) {
        return selectCount(MesWmArrivalNoticeDO::getVendorId, vendorId);
    }

}
