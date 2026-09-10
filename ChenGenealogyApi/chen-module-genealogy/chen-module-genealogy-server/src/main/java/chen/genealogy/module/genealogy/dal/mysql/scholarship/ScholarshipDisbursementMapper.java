package chen.genealogy.module.genealogy.dal.mysql.scholarship;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.DisbursementPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipDisbursementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScholarshipDisbursementMapper extends BaseMapperX<ScholarshipDisbursementDO> {

    default PageResult<ScholarshipDisbursementDO> selectPage(DisbursementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ScholarshipDisbursementDO>()
                .eqIfPresent(ScholarshipDisbursementDO::getApplicationId, reqVO.getApplicationId())
                .betweenIfPresent(ScholarshipDisbursementDO::getDisburseDate, reqVO.getDisburseDate())
                .orderByDesc(ScholarshipDisbursementDO::getId));
    }

    default ScholarshipDisbursementDO selectByApplicationId(Long applicationId) {
        return selectOne(new LambdaQueryWrapperX<ScholarshipDisbursementDO>()
                .eq(ScholarshipDisbursementDO::getApplicationId, applicationId)
                .orderByDesc(ScholarshipDisbursementDO::getId)
                .last("LIMIT 1"));
    }
}
