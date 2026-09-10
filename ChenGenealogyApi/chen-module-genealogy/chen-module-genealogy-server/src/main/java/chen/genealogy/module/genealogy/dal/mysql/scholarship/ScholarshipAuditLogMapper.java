package chen.genealogy.module.genealogy.dal.mysql.scholarship;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.module.genealogy.dal.dataobject.scholarship.ScholarshipAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScholarshipAuditLogMapper extends BaseMapperX<ScholarshipAuditLogDO> {

    default List<ScholarshipAuditLogDO> selectListByApplicationId(Long applicationId) {
        return selectList(ScholarshipAuditLogDO::getApplicationId, applicationId);
    }
}
