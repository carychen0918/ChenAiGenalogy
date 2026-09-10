package chen.genealogy.module.genealogy.dal.mysql.scholarship;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScholarshipConfigMapper extends BaseMapperX<ScholarshipConfigDO> {

    default ScholarshipConfigDO selectByYear(Long familyId, Integer year) {
        return selectOne(new LambdaQueryWrapperX<ScholarshipConfigDO>()
                .eq(ScholarshipConfigDO::getFamilyId, familyId)
                .eq(ScholarshipConfigDO::getYear, year)
                .orderByDesc(ScholarshipConfigDO::getId)
                .last("LIMIT 1"));
    }

    default ScholarshipConfigDO selectLatest(Long familyId) {
        return selectOne(new LambdaQueryWrapperX<ScholarshipConfigDO>()
                .eq(ScholarshipConfigDO::getFamilyId, familyId)
                .orderByDesc(ScholarshipConfigDO::getYear)
                .last("LIMIT 1"));
    }
}
