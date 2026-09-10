package chen.genealogy.module.genealogy.dal.mysql.scholarship;

import chen.genealogy.framework.common.pojo.PageResult;
import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.controller.admin.scholarship.vo.ScholarshipPageReqVO;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipApplicationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScholarshipApplicationMapper extends BaseMapperX<ScholarshipApplicationDO> {

    default PageResult<ScholarshipApplicationDO> selectPage(ScholarshipPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ScholarshipApplicationDO>()
                .eqIfPresent(ScholarshipApplicationDO::getYear, reqVO.getYear())
                .eqIfPresent(ScholarshipApplicationDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ScholarshipApplicationDO::getType, reqVO.getType())
                .eqIfPresent(ScholarshipApplicationDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ScholarshipApplicationDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(ScholarshipApplicationDO::getProvinceId, reqVO.getProvinceId())
                .eqIfPresent(ScholarshipApplicationDO::getCityId, reqVO.getCityId())
                .eqIfPresent(ScholarshipApplicationDO::getCountyId, reqVO.getCountyId())
                .eqIfPresent(ScholarshipApplicationDO::getCurrentAuditLevel, reqVO.getCurrentAuditLevel())
                .likeIfPresent(ScholarshipApplicationDO::getSchool, reqVO.getSchool())
                .likeIfPresent(ScholarshipApplicationDO::getApplyNo, reqVO.getApplyNo())
                .orderByDesc(ScholarshipApplicationDO::getId));
    }

    default ScholarshipApplicationDO selectByMemberAndYear(Long memberId, Integer year) {
        return selectOne(new LambdaQueryWrapperX<ScholarshipApplicationDO>()
                .eq(ScholarshipApplicationDO::getMemberId, memberId)
                .eq(ScholarshipApplicationDO::getYear, year)
                .in(ScholarshipApplicationDO::getStatus, 0, 1, 2, 3, 4, 5, 10, 11, 12, 13)
                .orderByDesc(ScholarshipApplicationDO::getId)
                .last("LIMIT 1"));
    }
}
