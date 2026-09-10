package chen.genealogy.module.genealogy.dal.mysql.activity;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.activity.TombSiteDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TombSiteMapper extends BaseMapperX<TombSiteDO> {

    default TombSiteDO selectByFamilyId(Long familyId) {
        return selectOne(new LambdaQueryWrapperX<TombSiteDO>()
                .eq(TombSiteDO::getFamilyId, familyId)
                .orderByDesc(TombSiteDO::getId)
                .last("LIMIT 1"));
    }
}
