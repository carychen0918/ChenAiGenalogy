package chen.genealogy.module.genealogy.dal.mysql.region;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.region.AdminRegionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminRegionMapper extends BaseMapperX<AdminRegionDO> {

    default AdminRegionDO selectByUserAndLevel(Long userId, String auditLevel) {
        return selectOne(new LambdaQueryWrapperX<AdminRegionDO>()
                .eq(AdminRegionDO::getUserId, userId)
                .eq(AdminRegionDO::getAuditLevel, auditLevel)
                .last("LIMIT 1"));
    }

    default List<AdminRegionDO> selectListByUserId(Long userId) {
        return selectList(AdminRegionDO::getUserId, userId);
    }

    default boolean existsAdmin(String auditLevel, Integer areaId) {
        if (areaId == null) {
            return false;
        }
        return selectCount(new LambdaQueryWrapperX<AdminRegionDO>()
                .eq(AdminRegionDO::getAuditLevel, auditLevel)
                .eq(AdminRegionDO::getAreaId, areaId)) > 0;
    }

    default List<AdminRegionDO> selectListByLevelAndArea(String auditLevel, Integer areaId) {
        if (areaId == null || auditLevel == null) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<AdminRegionDO>()
                .eq(AdminRegionDO::getAuditLevel, auditLevel)
                .eq(AdminRegionDO::getAreaId, areaId));
    }
}
