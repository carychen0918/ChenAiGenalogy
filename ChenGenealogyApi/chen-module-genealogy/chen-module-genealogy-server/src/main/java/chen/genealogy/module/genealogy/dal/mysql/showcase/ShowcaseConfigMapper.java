package chen.genealogy.module.genealogy.dal.mysql.showcase;

import chen.genealogy.framework.mybatis.core.mapper.BaseMapperX;
import chen.genealogy.framework.mybatis.core.query.LambdaQueryWrapperX;
import chen.genealogy.module.genealogy.dal.dataobject.showcase.ShowcaseConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShowcaseConfigMapper extends BaseMapperX<ShowcaseConfigDO> {

    default ShowcaseConfigDO selectByFamilyId(Long familyId) {
        return selectOne(new LambdaQueryWrapperX<ShowcaseConfigDO>()
                .eq(ShowcaseConfigDO::getFamilyId, familyId)
                .orderByDesc(ShowcaseConfigDO::getId)
                .last("LIMIT 1"));
    }
}
