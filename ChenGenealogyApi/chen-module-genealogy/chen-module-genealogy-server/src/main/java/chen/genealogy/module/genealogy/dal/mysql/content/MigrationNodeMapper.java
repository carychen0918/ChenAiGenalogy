package chen.genealogy.module.genealogy.dal.mysql.content;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.content.MigrationNodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MigrationNodeMapper extends BaseMapperX<MigrationNodeDO> {

    default List<MigrationNodeDO> selectListByFamilyId(Long familyId) {
        return selectList(new LambdaQueryWrapperX<MigrationNodeDO>()
                .eq(MigrationNodeDO::getFamilyId, familyId)
                .orderByAsc(MigrationNodeDO::getSort)
                .orderByAsc(MigrationNodeDO::getId));
    }
}
