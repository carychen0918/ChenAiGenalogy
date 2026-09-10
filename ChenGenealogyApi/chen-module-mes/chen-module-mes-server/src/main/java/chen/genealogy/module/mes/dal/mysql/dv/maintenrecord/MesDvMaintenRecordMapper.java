package chen.genealogy.module.mes.dal.mysql.dv.maintenrecord;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordPageReqVO;
import chen.genealogy.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 设备保养记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesDvMaintenRecordMapper extends BaseMapperX<MesDvMaintenRecordDO> {

    default PageResult<MesDvMaintenRecordDO> selectPage(MesDvMaintenRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvMaintenRecordDO>()
                .eqIfPresent(MesDvMaintenRecordDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(MesDvMaintenRecordDO::getMachineryId, reqVO.getMachineryId())
                .eqIfPresent(MesDvMaintenRecordDO::getUserId, reqVO.getUserId())
                .betweenIfPresent(MesDvMaintenRecordDO::getMaintenTime, reqVO.getMaintenTime())
                .orderByDesc(MesDvMaintenRecordDO::getId));
    }

    default Long selectCountByMachineryId(Long machineryId) {
        return selectCount(MesDvMaintenRecordDO::getMachineryId, machineryId);
    }

}
